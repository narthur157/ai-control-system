package server;

import java.io.IOException;

public class Tester {
	private final int CHANGE_DISTURBANCE = 101;

	private final double DESIRED_SPEED = 32.0;  
	
	private BrickComm comm;
	private Logger logger;
	private MotorController controller;
	
	public Tester(BrickComm commInit, Logger loggerInit) {
		comm = commInit;
		logger = loggerInit;
	}
	
	public Tester(BrickComm commInit, Logger loggerInit, MotorController controllerInit) {
		// call the other constructor
		this(commInit, loggerInit);
		controller = controllerInit;
	}
	
	public void runTest() throws IOException {
		logger.logln(controller.toString());

		int numLoops = 1000;
		for (int i=0;i<numLoops;++i) {

			//sends a power level for the disturbance wheel within +/-50% of desired speed
			// change disturbance wheel power randomly
			comm.sendInt(CHANGE_DISTURBANCE);

			//reads the new disturbance power level generated in the brick
			int disturbPower = comm.receiveInt();
			logger.logln("d\t" + disturbPower);    
			
			controller.setSpeed(DESIRED_SPEED);
		}	
	}
	
	public void collectDisturbanceData(int numLoops) throws IOException {
		for (int i=0;i<numLoops;++i) {

			//sends a power level for the disturbance wheel within +/-50% of desired speed
			// change disturbance wheel power randomly
			comm.sendInt(CHANGE_DISTURBANCE);

			//reads the new disturbance power level generated in the brick
			int disturbPower = comm.receiveInt();
			logger.logln("d\t" + disturbPower);    
		}	
	}
}
