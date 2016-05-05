package neural;

import java.io.IOException;

import lejos.util.Delay;

import communication.BrickComm;
import communication.BrickListener;
import communication.Command;
import framework.BrickState;
import neural.AnnClient;

public class NeuralController implements BrickListener {
	private BrickState bs;
	private final static int MIN_POWER = -100, 
							 MAX_POWER = 100, 
							 ERR_ALLOWANCE = 40, 
							 CONTROL_DELAY = 50;
	private int targetSpeed;
	private boolean speedSet = false;
	private boolean setting = false;
	private long startTime = -1;
	
	public NeuralController() {
		BrickComm.addListener(this);
	}
	
	public void updateBrick(BrickState bs) {
		this.bs = bs;
		
//		if (Math.abs(targetSpeed - bs.disturbSpeed) < ERR_ALLOWANCE) {
//			speedSet = true;
//		}
		
		if (!speedSet && !setting && (System.currentTimeMillis() - startTime) > CONTROL_DELAY) {
			// avoid trying to change before a previous loop finishes
			synchronized(this) {
				setting = true;
				
				try {
					startTime = System.currentTimeMillis();
					int power = findPower(targetSpeed);
					BrickComm.sendCommand(Command.CONTROL_WHEEL, power);
					BrickComm.sendCommand(Command.DISTURB_WHEEL, power);
					System.out.println("Current bs: " + bs.toString() + " found power " + power);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				setting = false;
			}
		}
	}
	
	public void setSpeed(int targetSpeed) throws IOException {
		// wait for a new brick update before setting the speed
		speedSet = false;
		this.targetSpeed = targetSpeed;
	}
	
	public int findPower(int targetSpeed) throws IOException {
		//int result = completeSearch(targetSpeed);
		//System.out.println("Attempting to set speed: " + targetSpeed);
		int result = AnnClient.searchSpeed(targetSpeed, bs);
		//System.out.println("Took " + (endTimer-beginTimer) + " ms to search for power");
		
		return result;
	}
	
	public int midpoint(int a, int b) {
		return (a+b)/2;
	}
	
	private int completeSearch(int targetSpeed) throws IOException {
		return completeSearchHelper(targetSpeed, -100, 666, -666);
	}
	
	private int completeSearchHelper(int targetSpeed, int testPower, double minErr, int bestPower) throws IOException {
		if (testPower <= MAX_POWER) {
			double computedErr = computeTestErr(targetSpeed, testPower);
			if (computedErr < minErr) {
				minErr = computedErr;
				bestPower	 = testPower;
			}
			return completeSearchHelper(targetSpeed, testPower+1, minErr, bestPower);
		}
		else {
			System.out.println("Best power found for target " + targetSpeed + " is " + bestPower + " with err " + minErr);
			return bestPower;
		}
	}
	
	private double computeTestErr(double targetSpeed, int testPower) throws IOException {
		double predictedSpeed = AnnClient.testInputs(testPower, bs)[0];
		return Math.abs(Math.abs(targetSpeed) - Math.abs(predictedSpeed));
	}
	
	/**
	 * This is unused because our neural net is currently not monotonic
	 * If we are able to achieve this, this search will give us O(lgn) 
	 * instead of O(n)
	 * So we put in our goal load speed annnnnd get out a power
	 * which achieves this load speed
	 * @param key - The power setting we're checking
	 * @param lowerBound - Max power setting
	 * @param upperBound - Min power setting
	 * @return - The power setting that supposedly gets us closest to our target in the next 5ms
	 * @throws IOException 
	 */
	private int binarySearch(int targetSpeed, int lowerBound, int upperBound) throws IOException {
		if (targetSpeed != this.targetSpeed) {
			System.out.println("Old speed not reached, interrupting for new speed");
			return findPower(this.targetSpeed);
		}
		
		// test if array is empty
		if (upperBound < lowerBound) {
			// set is empty, so return value showing not found
			System.err.println("No value found");
			// if anything should indicate something went wrong, the number 
			// of the devil is certainly a good candidate
			return 666;
		}
		else {
			// calculate midpoint to cut set in half
			int mid = midpoint(upperBound, lowerBound);
			double predictedSpeed = AnnClient.testInputs(mid, bs)[0];

			System.out.println("Power " + mid + " predicts " + predictedSpeed);
			
			// three-way comparison
			if (predictedSpeed + ERR_ALLOWANCE > targetSpeed) {
				// key is in lower subset
				return binarySearch(targetSpeed, lowerBound, mid - 1);
			}
			else if (predictedSpeed - ERR_ALLOWANCE < targetSpeed) {
				// key is in upper subset
				return binarySearch(targetSpeed, mid + 1, upperBound);
			}
			else {
				// key has been found
				return mid;
			}
		}
	}
}
