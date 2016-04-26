package datacollection;

import java.io.IOException;
import java.util.Random;

import communication.BrickComm;
import communication.Command;

import framework.Test;

public class SpeedTest extends Test {
		private int prevPower = 50;
		private Random rand = new Random();
		
		public SpeedTest() throws IOException {
			super();
		}

		@Override
		public void test() {
			int power;
			
			power = rand.nextInt(101);
//			
//			power = prevPower + 5;
//			
//			if (power > 100) power = 0;
//			
			BrickComm.sendCommand(Command.DISTURB_WHEEL, power); 
			BrickComm.sendCommand(Command.CONTROL_WHEEL, power); 
//			
//			prevPower = power;
		}
}
