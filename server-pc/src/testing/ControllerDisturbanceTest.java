package testing;

import java.io.IOException;
import java.util.Random;


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
		int disturbance = rand.nextInt(30);
		nc.setDisturbance(disturbance);
		
		int speed = count * 100;
		
		nc.setSpeed(speed);
		
		count = (count+1) % 10;
	}
}
