package client;

import lejos.nxt.LCD;

public class Receive {

	public static void main(String[] args) throws Exception {
		PCComm comm = new PCComm();
		BrickController bc = new BrickController(comm);
		StateUpdater updater = new StateUpdater(comm, bc);
		
		updater.start();
		bc.start();
		LCD.drawString("Controller done",  0,  3);
		updater.stopThread();
		LCD.drawString("Updater stopped", 0, 4);
		comm.close();
		LCD.drawString("Connections closed",  0,  5);
	}
}