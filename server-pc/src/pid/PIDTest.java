package pid;

import java.io.IOException;

import framework.MotorController;
import framework.Test;

public class PIDTest extends Test {
	private MotorController controller = new PIDController();
	
	public PIDTest() throws IOException {
		super();
		System.out.println("Creating PIDTest");
	}

	int count = 6;
	
	@Override
	public void test() {
		
		controller.setSpeed(100*count);
		
		count = (count+1) % 10;
	}

}
