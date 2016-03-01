package datacollection;

import communication.BrickComm;
import communication.BrickListener;
import framework.BrickState;

public class AdHoc implements BrickListener {
	
	/**
	 * Force the motor to 0 degrees
	 */
	public void holdPosition() {
		BrickComm.addListener(this);
	}
	
	public void updateBrick(BrickState bs) {
		
	}
}
