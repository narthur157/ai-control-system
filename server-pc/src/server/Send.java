package server;

import java.io.IOException;

//for use with Receive.java

import tests.DisturbanceTester;
import tests.Test;

public class Send {	
	public static void main(String[] args) throws IOException {
		BrickComm comm 		  	= new BrickComm();
		Test tester 			= new DisturbanceTester(comm);
		
		tester.collectDisturbanceData(1000);
		
		comm.close();
	}
}