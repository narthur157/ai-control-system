package pid;

import java.io.IOException;

import server.BrickComm;
import server.BrickState;
import server.MotorController;

public class PIDController implements MotorController {
	private BrickComm comm;
	private PIDLogger logger;
	
	// constants
	final private double P = 0.83, 	// proportional 
						 I = 0.28, 	// integral
						 D = 0.00,	// derivative	
						 MAX_OUTPUT = 8.0,
						 // limits to changes made in one cycle
						 MIN_OUTPUT = MAX_OUTPUT * -1,  
						 // acceptable range of speed = desiredSpeed +/- tolerance
						 ERR_TOLERANCE = 4.0;			
	
	private double 	prevError = 0.0,   		// used to compute derivative component
					totalError = 0.0, 		// used to compute integral component
					desiredSpeed = 0.0;
	
	private int cyclesStable = 0,			// keep track of how long speed stays in acceptable range
				testStable = 10;			// number of loops speed must remain in range to move on

	private BrickState bs = new BrickState(0, 0.0, 0, 0);
	
	public PIDController(BrickComm commInit, PIDLogger loggerInit) {
		comm = commInit;
		logger = loggerInit;
	}
	
	// get to a target speed - the main point of a motor controller
	public void setSpeed(double desiredSpeedVal) {
		desiredSpeed = desiredSpeedVal;
		//PID control loop
		boolean stable = false;
		while(!stable){
			double result = clamp(pidCalc(bs.currentSpeed, desiredSpeed), MIN_OUTPUT, MAX_OUTPUT);

			if (isStable(bs.currentSpeed, desiredSpeed)) { cyclesStable++; }
			else { cyclesStable = 0; }

			// must stay stable for testStable cycles
			if (cyclesStable >= testStable) {
				stable = true;
				cyclesStable = 0;
			}
			
			comm.sendInt((int) result);

			logger.logln(bs.toString());
			System.out.println("clamped PID output: " + result);
		}
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
	
	public String toString() {
		return "" + desiredSpeed + '\t' + P + '\t' + I + '\t' + D;
	}
}