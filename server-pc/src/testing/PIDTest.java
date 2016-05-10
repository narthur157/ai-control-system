package testing;

import java.io.IOException;

import pid.PIDController;


import framework.MotorController;

public class PIDTest extends Test {
	private MotorController controller = new PIDController();
	
	public PIDTest() throws IOException {
		super();
	}

	int count = 6;
	
	@Override
	public void test() {
		
		controller.setSpeed(100*count);
		
		count = (count+1) % 10;
	}

}
