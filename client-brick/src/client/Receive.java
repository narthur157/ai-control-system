package client;

public class Receive {

	public static void main(String[] args) throws Exception {
		PCComm comm = new PCComm();
		BrickController bc = new BrickController(comm);
		bc.start();
		comm.close();
	}
}