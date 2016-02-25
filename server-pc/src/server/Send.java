package server;

import java.io.IOException;

//for use with Receive.java

import tests.DataCollector;
import tests.Test;

public class Send {	
	public static void main(String[] args) {
		BrickComm comm = new BrickComm();
		
		try {
			Test tester = new DataCollector(comm);
			
			// start receiving messages from the brick
			comm.start();
			tester.runTest(3);
		}
		catch (IOException e) {
			
		}
		finally {
			comm.stopBrick();
			comm.close();
		}
	}
}