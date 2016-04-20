package client;

import java.io.IOException;

import lejos.util.Stopwatch;

public class StateUpdater extends Thread {
	private static final int UPDATE_DELAY = 5;
	private PCComm comm;
	private BrickController bc;
	private Stopwatch sendTimer = new Stopwatch();
	
	public StateUpdater(PCComm commInit, BrickController bcInit) {
		comm = commInit;
		bc = bcInit;
	}

	// Asynchronously send back updates to PC about our state instead of 
	// responding to requests. Too slow for the PC to request updates
	// and we should always want them anyways
	public void run() {
		try {
			sendTimer.reset();
			while (!isInterrupted()) {
				comm.sendBrick(bc.getState());
				// update every 5ms
				// sendBrick may take 
				if (UPDATE_DELAY - sendTimer.elapsed() > 0) {
					Thread.sleep(UPDATE_DELAY - sendTimer.elapsed());
				}
			}
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		catch (InterruptedException e) {
			// we probably did this ourselves, so just stop
		}
	}
	
	public void stopThread() {
		this.interrupt();
	}
}
