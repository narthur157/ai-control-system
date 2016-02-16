package server;

import java.io.IOException;

public class PIDController {
	
	private BrickComm comm;
	private PIDLogger logger;
	private final int CHANGE_DISTURBANCE = 101;
	
	// constants
	final private double P = 0.83, 	// proportional 
						 I = 0.28, 	// integral
						 D = 0.00,	// derivative	
						 MAX_OUTPUT = 8.0,
						 // limits to changes made in one cycle
						 MIN_OUTPUT = MAX_OUTPUT * -1,  
						 // acceptable range of speed = desiredSpeed +/- tolerance
						 ERR_TOLERANCE = 4.0,
						 DESIRED_SPEED = 32.0;  			
	
	private double 	prevError = 0.0,   		// used to compute derivative component
					totalError = 0.0; 		// used to compute integral component
	
	private int cyclesStable = 0,			// keep track of how long speed stays in acceptable range
				testStable = 10;			// number of loops speed must remain in range to move on

	private BrickState bs = new BrickState(0, 0.0, 0, 0);
	
	public PIDController(BrickComm commInit, PIDLogger loggerInit) {
		comm = commInit;
		logger = loggerInit;
	}
	
	// return val between min and max
	private double clamp(double val, double min, double max) {
		// ternary operator is (condition) ? (if true, return this) : (else, return this)
		return val >= max ? max :
					val <= min ? min :
						val;			
	}
	
	private boolean inRange(double val, double min, double max) {
		return val >= min && val <= max;
	}
	
	private boolean isStable(double currentSpeed, double desiredSpeed) {
		double desiredMin = desiredSpeed - ERR_TOLERANCE;
		double desiredMax = desiredSpeed + ERR_TOLERANCE;
		
		return inRange(currentSpeed, desiredMin, desiredMax);
	}
	
	// modifies totalError and prevError
	private double pidCalc(double currentSpeed, double desiredSpeed) {
		double error = desiredSpeed - bs.currentSpeed;
		
		totalError += error;
		double result = P * error + I * totalError + D * (error - prevError);
		prevError = error;
		
		return result;
	}
	
	// loop pid controller until stable
	private void pidLoop(double desiredSpeed) {
		//PID control loop
		boolean stable = false;
		while(!stable){
			double result = clamp(pidCalc(bs.currentSpeed, desiredSpeed), MIN_OUTPUT, MAX_OUTPUT);

			if (isStable(bs.currentSpeed, desiredSpeed)) { cyclesStable++; }
			else { cyclesStable = 0; }

			// must stay stable for testStable cycles
			if (cyclesStable >= testStable){
				stable = true;
				cyclesStable = 0;
			}
			
			comm.sendInt((int) result);

			logger.logln(bs.toString());
			System.out.println("clamped PID output: " + result);
		}
	}

	public void runTest() throws IOException {
		logger.logln("" + DESIRED_SPEED + '\t' + P + '\t' + I + '\t' + D);

		int numLoops = 1000;
		for (int i=0;i<numLoops;++i) 
		{

			//sends a power level for the disturbance wheel within +/-50% of desired speed
			//note: try just sending a set nonsense int (ie 101) and gen rand # in brick
			//update: sends 101 to signal brick to change the disturbance wheel power
			comm.sendInt(CHANGE_DISTURBANCE);

			//reads the new disturbance power level generated in the brick
			int disturbPower = comm.receiveInt();
			logger.logln("d\t" + disturbPower);    
			
			pidLoop(DESIRED_SPEED);
		}	
	}
}