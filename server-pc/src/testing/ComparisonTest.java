package testing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import neural.NeuralController;

import pid.PIDController;

import framework.MotorController;

public class ComparisonTest extends Test {
	private MotorController currentMotor;
	
	private final int SPEED = 600;
	private final int MAX_DISTURBANCE = 25;
	
	private ArrayList<Integer> disturbances;
	private int disturbanceIndex = 0;
	private Random rand = new Random();
	private long beginTime = -1;

	/**
	 * It is assumed that this is used for the neural/pid controllers, however
	 * it will actually work for any pair.
	 * 
	 * @param aIn Assumed to be neural controller
	 * @param bIn Assumed to be PID controller
	 * @throws IOException
	 */
	public ComparisonTest() throws IOException {
		super();
		
		currentMotor = new NeuralController();
		
		logger = new ComparisonLogger("neural.csv");
	}

	@Override
	protected void test() {
		if (disturbances == null) {
			disturbances = generateTest();
		}
		
		// switch to the other controller halfway
		if (curTestCount == (numTests/2)+1) {
			currentMotor.stop();
			currentMotor = null;
			
			currentMotor = new PIDController();
			
			disturbanceIndex = 0;
			logger.close();
			
			try {
				logger = new ComparisonLogger("pid.csv");
			} catch (IOException e) {
				e.printStackTrace();
				finishTest();
			}
			
			System.out.println("Neural test finished, testing with PID");
		}
		
		if (disturbanceIndex == 0) {
			beginTime = bs.time;
		}
		
		int disturbance = disturbances.get(disturbanceIndex);
		
		currentMotor.setDisturbance(disturbance);
		currentMotor.setSpeed(SPEED);
		
		disturbanceIndex++;
	}
	
	@Override
	protected String collectData() {
		if (beginTime == -1) return "";
		
		return (bs.time - beginTime) + "\t" + bs.disturbSpeed + "\t" + disturbances.get(disturbanceIndex) + "\t";
	}
	
	/**
	 * This doesn't have to be random. For the paper, it may be better
	 * to have this be some predetermined set of disturbances
	 * @return A list of disturbance offsests long enough for the test
	 */
	private ArrayList<Integer> generateTest() {
		System.out.println("numTest: " + numTests);
		ArrayList<Integer> result = new ArrayList<Integer>();
		
		// This actually generates more than we need, but it doesn't matter
		for(int i = 0; i < numTests; i++) {
			result.add(rand.nextInt(MAX_DISTURBANCE));
		}
		
		return result;
	}

}
