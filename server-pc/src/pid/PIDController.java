package pid;

import communication.BrickComm;
import communication.BrickListener;
import communication.Command;

import framework.BrickState;
import framework.MotorController;

// this currently is quite broken/requires almost total re-write
public class PIDController implements MotorController, BrickListener {
	private BrickComm comm;
	
	// constants
	final private double P = 1.83, 	// proportional 
						 I = 0.48, 	// integral
						 D = 0.20,	// derivative	
						 MAX_OUTPUT = 100.0,
						 // limits to changes made in one cycle
						 MIN_OUTPUT = MAX_OUTPUT * -1,  
						 // acceptable range of speed = desiredSpeed +/- tolerance
						 ERR_TOLERANCE = 20.0,
						 SCALING = 0.05;			
	
	private double 	prevError = 0.0,   		// used to compute derivative component
					totalError = 0.0, 		// used to compute integral component
					desiredSpeed = 0.0;
	
	public PIDController() {
		BrickComm.addListener(this);
	}
	
	// modifies totalError and prevError
	private int pidCalc(double currentSpeed, double desiredSpeed) {
		double error = desiredSpeed - currentSpeed;
		
		error *= SCALING;
		
		totalError += error;
		
		// does no good to have an integral response outside power range
//		if (I*totalError > MAX_OUTPUT) totalError = I/MAX_OUTPUT;
//		if (I*totalError < MIN_OUTPUT) totalError = I/MIN_OUTPUT;
//		
		double result = P * error + I * totalError + D * (error - prevError);
		prevError = error;
		
		return (int) clamp(result, MIN_OUTPUT, MAX_OUTPUT);
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

	void programSpeed() {
		if (ERR_TOLERANCE - Math.abs(desiredSpeed - currentSpeed) > 0) return;
		
		int power = pidCalc(currentSpeed, desiredSpeed);
		
		System.out.println("desiredSpeed=" + desiredSpeed + " currentSpeed=" + currentSpeed + " power=" + power);
		
		if (power > 100) power = 100;
		if (power < -100) power = -100;
		
		BrickComm.sendCommand(Command.CONTROL_WHEEL, power);
		BrickComm.sendCommand(Command.DISTURB_WHEEL, power);
	}
	
	public void setSpeed(double s) {
		desiredSpeed = s;
		System.out.println("Set desired speed " + s);
		programSpeed();
	}

	private double currentSpeed;
	private int update_now;
	
	public void updateBrick(BrickState bs) {
		currentSpeed = bs.disturbSpeed;
		if (update_now <= 0) {
			System.out.println("Got new speed " + currentSpeed);
			programSpeed();
			update_now = 10;
		}
		update_now--;
	}
	
	public String toString() {
		return "" + desiredSpeed + '\t' + P + '\t' + I + '\t' + D;
	}
}