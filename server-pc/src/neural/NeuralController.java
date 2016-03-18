package neural;

import java.io.IOException;

import communication.BrickComm;
import communication.BrickListener;
import communication.Command;
import framework.BrickState;
import neural.AnnClient;

public class NeuralController implements BrickListener {
	private BrickState bs;
	private final static int MIN_POWER = -100, MAX_POWER = 100, ERR_ALLOWANCE = 30;
	private int targetSpeed;
	private boolean speedSet = false;
	
	public NeuralController() {
		BrickComm.addListener(this);
	}
	
	public void updateBrick(BrickState bs) {
		this.bs = bs;
		
		if (!speedSet) {
			try {
				BrickComm.sendCommand(Command.CONTROL_WHEEL, findPower(targetSpeed));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void setSpeed(int targetSpeed) throws IOException {
		// wait for a new brick update before setting the speed
		speedSet = false;
		this.targetSpeed = targetSpeed;
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
			int mid = midpoint(upperBound, lowerBound);;
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
