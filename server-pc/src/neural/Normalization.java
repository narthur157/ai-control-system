package neural;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Normalization {
	private static double loadSpdStdDev, loadSpdMean,
				  		  angleStdDev, angleMean, 
				  		  controlPwrStdDev, controlPwrMean;
	
	static {
        Scanner scan;
        File file = new File("normalizationData");
        try {
            scan = new Scanner(file);
           
			loadSpdMean = scan.nextDouble();
			loadSpdStdDev = scan.nextDouble();
			angleMean = scan.nextDouble();
			angleStdDev = scan.nextDouble();
			controlPwrMean = scan.nextDouble();
			controlPwrStdDev = scan.nextDouble();
			
        } catch (FileNotFoundException e1) {
                e1.printStackTrace();
        }
	}
	
	public static double normalizeLoad(double load) {
		return normalize(load, loadSpdMean, loadSpdStdDev);
	}
	
	public static double normalizeAngle(double angle) {
		return normalize(angle, angleMean, angleStdDev);
	}
	
	public static double normalizeControl(double ctrl) {
		return normalize(ctrl, controlPwrMean, controlPwrStdDev);
	}
	
	public static double denormalizeLoad(double load) {
		return denormalize(load, loadSpdMean, loadSpdStdDev);
	}
	
	public static double denormalizeAngle(double angle) {
		return denormalize(angle, angleMean, angleStdDev);
	}
	
	public static double denormalizeControl(double ctrl) {
		return denormalize(ctrl, controlPwrMean, controlPwrStdDev);
	}
	
	private static double normalize(double val, double mean, double stdDev) {
		return (val - mean) / stdDev;
	}
	
	private static double denormalize(double val, double mean, double stdDev) {
		return (val * stdDev) + mean;
	}
}
