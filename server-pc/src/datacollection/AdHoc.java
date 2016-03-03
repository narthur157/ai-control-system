package datacollection;

import communication.BrickComm;
import communication.BrickListener;
import framework.BrickState;

// unused -- left temporarily as a reference for the PID implementation
public class AdHoc implements BrickListener {
	
	private static final int INCREMENT = 1,
							 ERR_ALLOWANCE = 3;
	private byte motor;
	private int holdPos;
	
	public AdHoc(byte motorInit) {
		motor = motorInit;
	}
	
	/**
	 * Force the motor maintain a given degree
	 */
	public void holdPosition(int position) {
		holdPos = position;
		BrickComm.addListener(this);
	}
	
	public void updateBrick(BrickState bs) {
		System.out.println(bs.toString());
		int power = bs.getMotorPower(motor);
		
		int newPower = power + (INCREMENT * (holdPos > bs.angle ? 1 : -1));
		
		System.out.println("newPower: " + newPower + " torqueAngle: " + bs.angle);
		
		if (Math.abs(Math.abs(bs.angle) - Math.abs(holdPos)) > ERR_ALLOWANCE) {
			BrickComm.sendCommand(motor, newPower);
		}
		else if (power != 0){
			// stop moving the motor when we don't need to
			BrickComm.sendCommand(motor, 0);
		}
	}
	
	public void stop() {
		BrickComm.rmListener(this);
	}
}
