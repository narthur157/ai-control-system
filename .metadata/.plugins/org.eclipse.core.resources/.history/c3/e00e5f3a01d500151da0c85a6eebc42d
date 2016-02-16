package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import lejos.pc.comm.NXTCommLogListener;
import lejos.pc.comm.NXTConnector;


//for use with Receive.java

public class Send {	
	public static void main(String[] args) throws IOException {
		PIDLogger logger 		= new PIDLogger();
		BrickComm comm 		  	= new BrickComm();
		PIDController pidCont 	= new PIDController(comm, logger);
		
		pidCont.runTest();
		
		comm.close();
	}
}