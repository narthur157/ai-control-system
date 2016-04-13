package datacollection;

import java.io.IOException;
import java.util.Random;

import communication.BrickComm;
import communication.Command;

import framework.Test;


public class DataCollector extends Test {
	
	private Random rand = new Random();
	private int count = 0;
	private int prevCtrlPwr = 50;
	private int prevDisturbPwr = 50;
	
	public DataCollector() throws IOException {
		super();
	}

	@Override
	public void test() {
		changeFlag = 0;
		int power;
		if (count == 0) {
			power = prevDisturbPwr + 5;
			if (power > 100) power = 0;
			BrickComm.sendCommand(Command.DISTURB_WHEEL, power); 
			prevDisturbPwr = power;
			changeFlag = 2;
		} else {		
			power = getNextPower(prevDisturbPwr);
			BrickComm.sendCommand(Command.CONTROL_WHEEL, power);
			changeFlag = 1;
			prevCtrlPwr = power;
		}
		
//		switch (count) {
//			case 0:
//				power = getNextPower(prevDisturbPwr);
//				BrickComm.sendCommand(Command.CONTROL_WHEEL, power);
//				changeFlag = 1;
//				prevCtrlPwr = power;
//				break;
//			case 1:
//				power = getNextPower(prevCtrlPwr);
//				// negate this since wheels face opposite directions
//				changeFlag = 2;
//				// disturbance is defined as any speed slower than the drive wheel
//				// positive power now goes forward, direction set on brick
//				BrickComm.sendCommand(Command.DISTURB_WHEEL, power); 
//				prevDisturbPwr = power;
//				break;
//		}
		
		count = (count+1) % 6;
	}
	
	private int getNextPower(int prevPower) {
		int randPower = prevPower+rand.nextInt(81)-40; 
		
		if (randPower < 0) {
			randPower = rand.nextInt(40);
		}
		if (randPower > 100) {
			randPower = 100-rand.nextInt(40);
		}
		return randPower;
	}

}
