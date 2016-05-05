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
	private static final int NUM_COLUMNS = 4;
	private static final int LOAD_SPEED_INDEX = 0;
	private static final int ANGLE_INDEX = 1;
	private static final int POWER_INDEX = 2;
	private static final int PREDICTED_SPEED_INDEX = 3;

	private ArrayList<ArrayList<Row>> dataset = new ArrayList<ArrayList<Row>>();
	
	private int max_size = 0;
	private double[] sums = new double[NUM_COLUMNS];
	private double[] vars = new double[NUM_COLUMNS];
	
	
	private class Row {
		int[] columns = new int[NUM_COLUMNS];
		// loadspd angle control future-loadspd
	}

	private void computeMean() {
		int count = 0;
		
		for (ArrayList<Row> bucket : dataset) {
			for (Row r : bucket) {
				
				for (int colIndex=0; colIndex < NUM_COLUMNS; colIndex++) {
					sums[colIndex] += r.columns[colIndex];
					count++;
				}
			}
		}
		
		for (int colIndex = 0; colIndex < NUM_COLUMNS; colIndex ++) {
			sums[colIndex ] /= count;
		}
		
		count = 0;

		for (ArrayList<Row> bucket : dataset) {
			for (Row r : bucket) {
				for (int k = 0; k < r.columns.length; k++) {
					double v = r.columns[k];
					v -= sums[k];
					vars[k] += v*v;
					count++;
				}
			}
		}
		
		for (int k = 0; k < NUM_COLUMNS; k++) {
			vars[k] = Math.sqrt(vars[k] / count);
		}
	}
	
	private Row parseRow(String line) {
		String[] fields = line.split("\\s+");
		Row r = new Row();
		
		for (int colIndex = 0; colIndex < NUM_COLUMNS; colIndex++) {
			r.columns[colIndex] = (int) Math.round(Double.parseDouble(fields[colIndex]));
		}
		
		return r;
	}
	
	
	private void readFile(String fname) throws Exception {
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
	
	private void balanceDataset() {
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
	
	private ArrayList<ArrayList<Double>> normalize() {
		ArrayList<ArrayList<Double>> normalizedOutput = new ArrayList<ArrayList<Double>>();
		
		for (ArrayList<Row> bucket : dataset) {
			for (Row row : bucket) {
				ArrayList<Double> outputRow = new ArrayList<Double>();
				
				for (int k = 0; k < NUM_COLUMNS; k++) {
					int normalizationIndex = k;

					 if (normalizationIndex == LOAD_SPEED_INDEX) {
						 normalizationIndex = PREDICTED_SPEED_INDEX;
					 }

					double val = row.columns[k], 
						   mean = sums[normalizationIndex], 
						   stdDev = vars[normalizationIndex];
						
					outputRow.add(Normalization.normalize(val, mean, stdDev));
				}
				
				normalizedOutput.add(outputRow);
			}
		}
	
		return normalizedOutput;
	}
	
	private void dumpFile(String fname, ArrayList<ArrayList<Double>> data) throws IOException {
		PrintWriter pw = new PrintWriter(new FileWriter(fname));
		
//		int subset_size = max_size;
//		int startIndex, endIndex;
//		
//		if (!isValidationSet) {
//			startIndex = 0;
//			endIndex = (int) (subset_size * 0.8);
//		} 
//		else {
//			startIndex = (int) (subset_size * 0.8);
//			endIndex = subset_size;
//		}
		
		for(ArrayList<Double> row : data) {
			String line = "";
			
			// hack to make this work without changing train
//			line += 0 + "\t";
			
			for (Double d : row) {
				line += d + "\t";
			}
			
			pw.println(line.trim());
		}
		
		pw.close();
	}
	
	private void dumpMeanStd(String fname) throws IOException {
		PrintWriter pw = new PrintWriter(new FileWriter(fname));
		System.out.println("Wrote normalization data to " + fname);
		
		pw.println(sums[PREDICTED_SPEED_INDEX] + "\t" + vars[PREDICTED_SPEED_INDEX]);
		// angle is not a part of this
		pw.println(sums[ANGLE_INDEX] + "\t" + vars[ANGLE_INDEX]);
		pw.println(sums[POWER_INDEX] + "\t" + vars[POWER_INDEX]);
		pw.close();
	}
	
	public static void main(String[] args) throws Exception {
		RebalanceData rd = new RebalanceData();
		
		rd.readFile("training-set.csv");
		
		//rd.balanceDataset();
		rd.computeMean();
		ArrayList<ArrayList<Double>> normalizedData = rd.normalize();
		
		rd.dumpFile("LightSpeedANN/train-set.csv", normalizedData);
		rd.dumpFile("LightSpeedANN/test-set.csv", normalizedData);
		
		System.out.println("Wrote train-set.csv and test-set.csv to LightSpeedANN");
		
		rd.dumpMeanStd("normalizationData");
	}
}
