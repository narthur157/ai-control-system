package server;

import java.io.IOException;

import communication.BrickComm;

import datacollection.DataCollector;
import framework.Test;

//for use with Receive.java


public class Send {	
	public static void main(String[] args) {
		runPidTest();
		//runDataTest();
	}
	
	public static void runPidTest() {
		BrickComm comm = new BrickComm();
	}
	
	public static void runDataTest() {
		BrickComm comm = new BrickComm();
		
		try {
			Test tester = new DataCollector(comm);
			
			// start receiving messages from the brick
			comm.start();
			tester.runTest(30);
		}
		catch (IOException e) {
			
		}
		finally {
			comm.stopBrick();
			comm.close();
		}
	}
}

