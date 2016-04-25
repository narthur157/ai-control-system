package datacollection;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import neural.Normalization;

public class RebalanceData {
	private static final int NUM_COLUMNS = 3;
	private static final int PREDICTED_SPEED_INDEX = 2;
	
	private class Row {
		int[] columns = new int[NUM_COLUMNS];
		// loadspd angle control future-loadspd
	}

	int max_size = 0;
	double[] sums = new double[NUM_COLUMNS];
	double[] vars = new double[NUM_COLUMNS];
	
	void computeMean() {
		int subset_size = max_size;
		int count = 0;
		
		for (int bucketIndex = 0; bucketIndex<dataset.size(); bucketIndex++) {
			if (dataset.get(bucketIndex).size() == 0) continue;
			
			for (int rowIndex = 0; rowIndex < subset_size; rowIndex++) {
				Row r = dataset.get(bucketIndex).get(rowIndex);
				
				for (int colIndex=0; colIndex < r.columns.length; colIndex++) {
					sums[colIndex] += r.columns[colIndex];
					count++;
				}
			}
		}
		
		for (int colIndex = 0; colIndex <NUM_COLUMNS; colIndex ++) {
			sums[colIndex ] /= count;
		}
		
		count = 0;
		
		for (int i=0; i<dataset.size(); i++) {
			if (dataset.get(i).size() == 0) continue;
			
			for (int j=0; j<subset_size; j++) {
				Row r = dataset.get(i).get(j);
				
				for (int k=0; k < r.columns.length; k++) {
					double v = r.columns[k];
					v -= sums[k];
					vars[k] += v*v;
					count++;
				}
			}
		}
		for (int k=0; k<NUM_COLUMNS; k++) {
			vars[k] = Math.sqrt(vars[k] / count);
		}
	}
	
	Row parseRow(String line) {
		String[] fields = line.split("\\s+");
		Row r = new Row();
		
		for (int colIndex=0; colIndex < NUM_COLUMNS; colIndex++) {
			r.columns[colIndex] = (int)Math.round(Double.parseDouble(fields[colIndex]));
		}
		
		return r;
	}
	
	ArrayList<ArrayList<Row>> dataset = new ArrayList<ArrayList<Row>>();
	
	void readFile(String fname) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(fname));
		
		// skip column headers, which is the first line
	    String line = br.readLine();
	    
	    while ((line = br.readLine()) != null) {
	       Row r = parseRow(line);
	       int ldSpd = r.columns[PREDICTED_SPEED_INDEX];
	       
	       if (ldSpd < 0) continue;
	       
	       while (dataset.size() < ldSpd+1) {
	    	   dataset.add(new ArrayList<Row>());
	       }
	       
	       ArrayList<Row> bucket = dataset.get(ldSpd);
	       bucket.add(r);
	    }
	    
		br.close();
	}
	
	void balanceDataset() {
		for (int i=0; i<dataset.size(); i++) {
			int s = dataset.get(i).size();
			
			if (s > max_size) max_size = s;
			
			long seed = System.nanoTime();
			Collections.shuffle(dataset.get(i), new Random(seed));
		}
		
		for (int i=0; i < dataset.size(); i++) {
			ArrayList<Row> subset = dataset.get(i);
			int orig_size = subset.size();
			int size = orig_size;
			
			if (size == 0) continue;
			
			while (size < max_size) {
				subset.add(subset.get(size % orig_size));
				size++;
			}
		}
	}
	
	void dumpFile(String fname, boolean isValidationSet) throws IOException {
		PrintWriter pw = new PrintWriter(new FileWriter(fname));
		
		int subset_size = max_size;
		int startIndex, endIndex;
		
		if (!isValidationSet) {
			startIndex = 0;
			endIndex = (int) (subset_size * 0.8);
		} 
		else {
			startIndex = (int) (subset_size * 0.8);
			endIndex = subset_size;
		}
		
		for (int rowIndex=startIndex; rowIndex < endIndex; rowIndex++) {
			for (int bucketIndex=0; bucketIndex<dataset.size(); bucketIndex++) {
				if (dataset.get(bucketIndex).size() == 0) continue;
				
				Row r = dataset.get(bucketIndex).get(rowIndex);
				
				pw.print((r.columns[0] - sums[0]) / vars[0]);
				
				for (int k=1; k<r.columns.length; k++) {
					int normalizationIndex = k;
					
					if (normalizationIndex == 3) normalizationIndex = 0;
					
					double 	val = r.columns[k],
							mean = sums[normalizationIndex],
							stdDev = vars[normalizationIndex];
					
					pw.print("\t" + Normalization.normalize(val, mean, stdDev));
				}
				pw.println();
			}
		}
		pw.close();
	}
	
	void dumpMeanStd(String fname) throws IOException {
		PrintWriter pw = new PrintWriter(new FileWriter(fname));
		System.out.println("Wrote normalization data to " + fname);
		
		pw.println(sums[0] + "\t" + vars[0]);
		pw.println(sums[1] + "\t" + vars[1]);
		pw.println(sums[2] + "\t" + vars[2]);
		pw.close();
	}
	
	public static void main(String[] args) throws Exception {
		RebalanceData rd = new RebalanceData();
		
		rd.readFile("training-set.csv");
		
		rd.balanceDataset();
		rd.computeMean();
		
		rd.dumpFile("LightSpeedANN/train-set.csv", false);
		rd.dumpFile("LightSpeedANN/test-set.csv", true);
		System.out.println("Wrote train-set.csv and test-set.csv to LightSpeedANN");
		
		rd.dumpMeanStd("normalizationData");
	}
}
