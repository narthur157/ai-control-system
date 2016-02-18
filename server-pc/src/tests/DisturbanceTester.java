package tests;

import java.io.IOException;
import java.util.Random;

import server.BrickComm;
import server.Command;

public class DisturbanceTester extends Test {

	Random rand = new Random();
	
	public DisturbanceTester(BrickComm commInit) throws IOException {
		super(commInit);
		logger = new FileLogger();
	}

	@Override
	public void test() throws IOException {
		int disturbPower = (rand.nextInt(41) + 35) * -1; //negate this since wheels face opposite directions
		comm.sendCommand(Command.DISTURB_WHEEL, disturbPower);
	}

}
