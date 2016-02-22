package client;

import java.io.IOException;

import lejos.util.Delay;

public class StateUpdater extends Thread {
	private PCComm comm;
	private BrickController bc;
	
	public StateUpdater(PCComm commInit, BrickController bcInit) {
		comm = commInit;
		bc = bcInit;
	}

	// Asynchronously send back updates to PC about our state instead of 
	// responding to requests. Too slow for the PC to request updates
	// and we should always want them anyways
	public void run() {
		try {
			while (true) {
				comm.sendBrick(bc.getState());
				Delay.msDelay(5);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
