package datacollection;

import java.io.IOException;

import communication.BrickComm;
import communication.Command;

import framework.Test;

public class SpeedTest extends Test {
		private int prevPower = -1;
		
		public SpeedTest() throws IOException {
			super();
		}

		@Override
		public void test() {
			int power;
		
			power = prevPower + 1;
			
			if (power > 100) power = 0;
			
			BrickComm.sendCommand(Command.DISTURB_WHEEL, power); 
			BrickComm.sendCommand(Command.CONTROL_WHEEL, power); 
			
			prevPower = power;
		}
}
