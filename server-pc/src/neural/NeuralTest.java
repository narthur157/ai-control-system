package neural;

import java.io.IOException;

import framework.MotorController;
import framework.Test;

public class NeuralTest extends Test {
	private MotorController nc = new NeuralController();
	
	int count = 6;
	
	public NeuralTest() throws IOException {
		super();
//		testLength = 4000;
	}

	@Override
	protected void test() {
		int speed = count * 100;
		System.out.println("Trying to set speed: " + speed);
		nc.setSpeed(speed);
		
		count = 6;//(count+1) % 10;
	}

}
