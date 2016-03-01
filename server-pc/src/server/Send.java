package server;

import java.io.IOException;

import pid.PIDTest;

import communication.BrickComm;

import datacollection.DataCollector;
import framework.Test;

//for use with Receive.java


public class Send {	
	public static void main(String[] args) {
		//runPidTest();
		runDataTest();
	}
	
	public static void runPidTest() {
		BrickComm comm = new BrickComm();

		try {
			Test tester = new PIDTest(comm);
			
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
	
	public static void runDataTest() {
		BrickComm comm = new BrickComm();
		
		try {
			Test tester = new DataCollector(comm);
			
			// start receiving messages from the brick
			comm.start();
			tester.runTest(5);
		}
		catch (IOException e) {
			
		}
		finally {
			comm.stopBrick();
			comm.close();
		}
	}
}

