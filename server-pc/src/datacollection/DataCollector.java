package datacollection;

import java.io.IOException;
import java.util.Random;

import communication.BrickComm;
import communication.Command;

import framework.Test;


public class DataCollector extends Test {
	
	private Random rand = new Random();
	private int count = 0;
	// impossible control value
	private int prevCtrlPwr = 666;
	
	public DataCollector() throws IOException {
		super();
	}

	@Override
	public void test() {
		changeFlag = 0;
		
		switch (count) {
			case 0:
				int randPower = 30+rand.nextInt(70); 
				changeFlag = 1;
				BrickComm.sendCommand(Command.CONTROL_WHEEL, randPower);
				BrickComm.sendCommand(Command.DISTURB_WHEEL, randPower);
				
				prevCtrlPwr = randPower;
				break;
			case 1:
				// negate this since wheels face opposite directions
				changeFlag = 2;
				// disturbance is defined as any speed slower than the drive wheel
				// positive power now goes forward, direction set on brick
				BrickComm.sendCommand(Command.DISTURB_WHEEL, prevCtrlPwr - rand.nextInt(40)); 
				break;
		}
		
		count = (count+1) % 2;
	}

}
