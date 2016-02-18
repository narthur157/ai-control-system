package server;

import java.io.IOException;
//for use with Receive.java

public class Send {	
	public static void main(String[] args) throws IOException {
		PIDLogger logger 		= new PIDLogger();
		BrickComm comm 		  	= new BrickComm();
		PIDController pidCont 	= new PIDController(comm, logger);
		Tester tester 			= new Tester(comm, logger, pidCont);
		
		tester.collectDisturbanceData(1000);
		
		comm.close();
	}
}