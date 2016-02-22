package client;

public class Receive {

	public static void main(String[] args) throws Exception {
		PCComm comm = new PCComm();
		BrickController bc = new BrickController(comm);
		StateUpdater updater = new StateUpdater(comm, bc);
		
		updater.start();
		bc.start();
		comm.close();
	}
}