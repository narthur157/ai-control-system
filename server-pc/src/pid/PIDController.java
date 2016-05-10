package pid;

import framework.MotorController;

public class PIDController extends MotorController {
	// constants
	final private double P = 1.83, 	// proportional 
						 I = 0.48, 	// integral
						 D = 0.20,	// derivative	
						 SCALING = 0.05;			
	
	private double 	prevError = 0.0,   		// used to compute derivative component
					totalError = 0.0; 		// used to compute integral component
	
	// modifies totalError and prevError
	private int pidCalc(double currentSpeed, double desiredSpeed) {
		double error = desiredSpeed - currentSpeed;
		
		error *= SCALING;
		
		totalError += error;
		
		// does no good to have an integral response outside power range
		if (I*totalError > MAX_OUTPUT) totalError = I/MAX_OUTPUT;
		if (I*totalError < MIN_OUTPUT) totalError = I/MIN_OUTPUT;
		
		double result = P * error + I * totalError + D * (error - prevError);
		prevError = error;
		
		return (int) clamp(result, MIN_OUTPUT, MAX_OUTPUT);
	}

	@Override
	protected int findPower() throws Exception {
		double currentSpeed = bs.disturbSpeed;
		
		double power = pidCalc(currentSpeed, targetSpeed);
		
		return (int) power;
	}
	
	@Override
	public String toString() {
		return "" + targetSpeed + '\t' + P + '\t' + I + '\t' + D;
	}
	
	// return val between min and max
	private double clamp(double val, double min, double max) {
		// ternary operator is (condition) ? (if true, return this) : (else, return this)
		return val >= max ? max :
					val <= min ? min :
						val;			
	}
}