package server;

import java.io.IOException;

import pid.PIDTest;

import communication.BrickComm;

import datacollection.DataCollector;
import framework.Test;

//for use with Receive.java

/**
 * The driver for the server side code
 * @author Nick Arthur
 */
public class Send {	
	public static void main(String[] args) {
		//runPidTest();
		runDataTest();
	}
	
	public static void runPidTest() {
		try {
			Test tester = new PIDTest();
			
			tester.runTest(30);
		}
		catch (IOException e) {
			
		}
		finally {
			BrickComm.stopBrick();
			BrickComm.close();
		}
	}
	
	public static void runDataTest() {
		try {
			Test tester = new DataCollector();
			
			tester.runTest(5);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			BrickComm.stopBrick();
			BrickComm.close();
		}
	}
}

