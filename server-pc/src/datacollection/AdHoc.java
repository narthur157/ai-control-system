package datacollection;

import communication.BrickComm;
import communication.BrickListener;
import framework.BrickState;

public class AdHoc implements BrickListener {
	private BrickComm comm;
	
	public AdHoc(BrickComm commInit) {
		comm = commInit;
	}
	/**
	 * Force the motor to 0 degrees
	 */
	public void holdPosition() {
		comm.addListener(this);
	}
	
	public void updateBrick(BrickState bs) {
		
	}
}
