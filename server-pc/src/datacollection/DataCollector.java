package datacollection;

import java.io.IOException;
import java.util.Random;

import communication.BrickComm;
import communication.Command;

import framework.Test;


public class DataCollector extends Test {

	private Random rand = new Random();
	private int count = 0;
	
	public DataCollector() throws IOException {
		super();
	}

	@Override
	public void test() throws IOException {
		changeFlag = 0;
		int randPower = (rand.nextInt(41) + 35); 
		
		switch (count) {
			case 0: 
				BrickComm.sendCommand(Command.CONTROL_WHEEL, randPower);
				changeFlag = 1;
				break;
			case 1:
				// negate this since wheels face opposite directions
				BrickComm.sendCommand(Command.DISTURB_WHEEL, randPower * -1); 
				changeFlag = 2;
				break;
		}
		
		count = (count+1) % 2;
	}

}
