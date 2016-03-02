package datacollection;

import communication.BrickComm;
import communication.BrickListener;
import framework.BrickState;

public class AdHoc implements BrickListener {
	
	private static final int INCREMENT = 10;
	private static final int ERR_ALLOWANCE = 0;
	private byte motor;
	
	public AdHoc(byte motorInit) {
		motor = motorInit;
	}
	
	/**
	 * Force the motor to 0 degrees
	 */
	public void holdPosition() {
		BrickComm.addListener(this);
	}
	
	public void updateBrick(BrickState bs) {
		int power = bs.getMotorPower(motor);
		
		int newPower = power + (INCREMENT * (power > 0 ? 1 : -1));
		
		if (Math.abs(bs.angle) > ERR_ALLOWANCE) {
			BrickComm.sendCommand(motor, newPower);
		}
		else if (bs.torquePower != 0){
			// stop moving the motor when we don't need to
			BrickComm.sendCommand(motor, 0);
		}
	}
	
	public void stop() {
		BrickComm.rmListener(this);
	}
}
