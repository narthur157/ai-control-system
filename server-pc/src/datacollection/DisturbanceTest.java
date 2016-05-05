package datacollection;

import java.io.IOException;
import java.util.Random;

import communication.BrickComm;
import communication.Command;

import framework.Test;

//Select this in server/Send.java
public class DisturbanceTest extends Test {
	private int prevPower = 10;
	private int powerChange = 1;
	private Random rand = new Random();
	
	public DisturbanceTest() throws IOException {
		super();
	}

	@Override
	public void test() {
		int power;
	
		power = prevPower + powerChange;
		
		if (power > 100) powerChange = -1;
		
		if (testCount > 203) {
			power = rand.nextInt(101);
		}
		
		BrickComm.sendCommand(Command.DISTURB_WHEEL, power); 
		BrickComm.sendCommand(Command.CONTROL_WHEEL, power); 
		
		prevPower = power;
	}
}
