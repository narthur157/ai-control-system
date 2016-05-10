package testing;

import java.io.IOException;
import java.util.Random;

import neural.NeuralController;


import framework.MotorController;

public class ControllerDisturbanceTest extends Test {
	private MotorController nc;
	
	private Random rand = new Random();
	
	int count = 6;
	
	public ControllerDisturbanceTest(MotorController nc) throws IOException {
		super();
		this.nc = nc;
//		testLength = 4000;
	}

	@Override
	protected void test() {
		int disturbance = rand.nextInt(20);
		nc.setDisturbance(disturbance);
		int speed = count * 100;
		System.out.println("Trying to set speed: " + speed + " disturbance: " + disturbance);
		nc.setSpeed(speed);
		
		count = (count+1) % 10;
	}

}
