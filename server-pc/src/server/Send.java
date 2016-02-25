package server;

import java.io.IOException;

//for use with Receive.java

import tests.DataCollector;
import tests.Test;

public class Send {	
	public static void main(String[] args) throws IOException {
		BrickComm comm 		  	= new BrickComm();
		Test tester 			= new DataCollector(comm);
		
		// start receiving messages from the brick
		comm.start();
		tester.runTest(1000);
		
		comm.close();
	}
}