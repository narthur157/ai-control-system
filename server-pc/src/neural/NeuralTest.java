package neural;

import java.io.IOException;

import framework.Test;

public class NeuralTest extends Test {
	private NeuralController nc = new NeuralController();
	
	int count = 3;
	
	public NeuralTest() throws IOException {
		super();
		testLength = 10000;
	}

	@Override
	protected void test() {
		try {
			int speed = count * 100;
			System.out.println("Trying to set speed: " + speed);
			nc.setSpeed(speed);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		count = count % 10;
	}

}
