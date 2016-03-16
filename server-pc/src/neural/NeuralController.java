package neural;

import java.io.IOException;

import communication.BrickComm;
import communication.BrickListener;
import communication.Command;
import framework.BrickState;
import neural.AnnClient;

public class NeuralController implements BrickListener {
	private BrickState bs;
	private final static int MIN_POWER = -100, MAX_POWER = 100;
	
	public void updateBrick(BrickState bs) {
		this.bs = bs;
	}
	
	public void setSpeed(int targetSpeed) throws IOException {
		BrickComm.sendCommand(Command.CONTROL_WHEEL, findPower(targetSpeed));
	}
	
	public int findPower(int targetSpeed) throws IOException {
		long beginTimer = System.currentTimeMillis();
		int result = binarySearch(targetSpeed, MIN_POWER, MAX_POWER);
		long endTimer = System.currentTimeMillis();
		System.out.println("Took " + (endTimer-beginTimer) + " ms to search for power");
		
		return result;
	}
	
	public int midpoint(int a, int b) {
		return (a+b)/2;
	}
	
	/**
	 * So we put in our goal load speed annnnnd get out a power
	 * which achieves this load speed
	 * @param key - The power setting we're checking
	 * @param lowerBound - Max power setting
	 * @param upperBound - Min power setting
	 * @return - The power setting that supposedly gets us closest to our target in the next 5ms
	 * @throws IOException 
	 */
	public int binarySearch(int targetSpeed, int lowerBound, int upperBound) throws IOException {
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
			int mid = midpoint(upperBound, lowerBound);;
			double predictedSpeed = AnnClient.testInputs(mid, bs)[0];

			System.out.println("Power " + mid + " predicts " + predictedSpeed);
			
			// three-way comparison
			if (predictedSpeed > targetSpeed) {
				// key is in lower subset
				return binarySearch(targetSpeed, lowerBound, mid - 1);
			}
			else if (predictedSpeed < targetSpeed) {
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
