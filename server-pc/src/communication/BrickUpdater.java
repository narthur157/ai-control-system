package communication;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;

import framework.BrickState;

/**
 * Observer pattern to send async brick updates to listeners
 * @author Nicholas Arthur
 *
 */
public class BrickUpdater extends Thread {
	// asynchronously receive updates from the brick
	private boolean finished = false;
	private DataInputStream inDat;
	
	ArrayList<BrickListener> listeners = new ArrayList<BrickListener>();
	
	public BrickUpdater(DataInputStream inDatInit) {
		inDat = inDatInit;
	}
	
	public void run() {
		while (!finished) {
			try {
				// blocking call
				update(readBrick());
			} catch (IOException e) {
				if (!finished) {
					System.err.println("Aborting brick reads");
				}
				break;
			}
		}
	}
	
	BrickState readBrick() throws IOException {
		//read values back from brick
		try {
			// could be optimized
			int time = inDat.readInt();
			double disturbSpeed = inDat.readDouble();
			int disturbPower = inDat.readInt();
			int controlPower = inDat.readInt();
			int torquePower = inDat.readInt();
			int angle = inDat.readInt();	
			
			return new BrickState(time, disturbSpeed, disturbPower, controlPower, torquePower, angle);
		} catch (IOException ioe) {
			// we force this exception when we call close, so avoid complaining
			// if we are doing this ourselves
			if (!finished) {
				System.err.println("IO Exception reading reply");
			}
			
			finishListeners();
			
			throw ioe;
		}
	}
	
	private void update(BrickState bs) {
		for (BrickListener l : listeners) {
			synchronized(l) {
				l.updateBrick(bs);
			}
		}
	}
	
	private void finishListeners() {
		for (BrickListener bl : listeners) {
			synchronized(bl) {
				bl.notify();
			}
		}	
	}

	public void stopUpdater() {
		finishListeners();
		finished = true;
	}

}
