package server;

import java.io.IOException;

public class PIDController {
	
	private BrickComm comm;
	private PIDLogger logger;
	final int CHANGE_DISTURBANCE = 101;
	//values for PID controller
	final double P = 0.83;		// proportional multiplier
	final double I = 0.28;		// integral multiplier
	final double D = 0.00; 		// derivative multiplier
	
	double maxOutput = 8.0;   	// limits to changes made in one cycle
	double minOutput = -8.0;   	
	double prevError = 0.0;   	// used to compute derivative component
	double totalError = 0.0; 	// used to compute integral component
	double tolerance = 4.0;  	// acceptable range of speed = desiredSpeed +/- tolerance
	double desiredSpeed = 32.0;	// set point
	double error = 0.0;			// used to compute proportional component
	double result = 0.0;		// amount to change driving wheel's power
	boolean stable = false;		// when stable, change disturbance and start again
	int cyclesStable = 0;		// keep track of how long speed stays in acceptable range
	int testStable = 10;			// number of loops speed must remain in range to move on

	//additional values to print each cycle (see line 127 for print statement)
	int disturbPower = 0;		//power of the disturbance wheel
	

	private BrickState bs = new BrickState(0, 0.0, 0, 0);
	
	public PIDController(BrickComm commInit, PIDLogger loggerInit) {
		comm = commInit;
		logger = loggerInit;
	}
	
	private double clamp(double val, double min, double max) {
		return val >= max ? max :
					val <= min ? min :
						val;			
	}
	
	private void pidLoop() {
		//PID control loop
		stable = false;
		while(!stable){
			//calculate control value
			error = desiredSpeed - bs.currentSpeed;
			totalError += error;
			//if(totalError * I > maxOutput) totalError = maxOutput/I;
			//else if(totalError * I < minOutput) totalError = minOutput/I;
			result = P * error + I * totalError + D * (error - prevError);
			prevError = error;

			result = clamp(result, minOutput, maxOutput);

			if (bs.currentSpeed >= desiredSpeed - tolerance && 
					bs.currentSpeed <= desiredSpeed + tolerance) { cyclesStable++; }
			else { cyclesStable = 0; }

			if (cyclesStable >= testStable){
				stable = true;
				cyclesStable = 0;
			}
			
			comm.sendInt((int) result);

			//print values to file
			logger.logln(bs.toString());
			System.out.println(result);
		}
	}

	public void runTest() {
		out.println("" + desiredSpeed + '\t' + P + '\t' + I + '\t' + D);

		int numLoops = 1000;
		for (int i=0;i<numLoops;++i) 
		{

			//sends a power level for the disturbance wheel within +/-50% of desired speed
			//note: try just sending a set nonsense int (ie 101) and gen rand # in brick
			//update: sends 101 to signal brick to change the disturbance wheel power
			comm.sendInt(CHANGE_DISTURBANCE);

			//reads the new disturbance power level generated in the brick
			disturbPower = comm.receiveInt();
			logger.logln("d\t" + disturbPower);    
			
			pidLoop();
		}	
	}
}