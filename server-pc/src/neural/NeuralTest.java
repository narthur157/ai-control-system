package neural;

import java.io.IOException;

import framework.Test;

public class NeuralTest extends Test {
	private NeuralController nc = new NeuralController();
	
	public NeuralTest() throws IOException {
		super();
	}

	@Override
	protected void test() {
		try {
			System.out.println("Trying to set speed");
			nc.setSpeed(50);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
