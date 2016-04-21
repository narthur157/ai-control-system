package server;

import java.io.IOException;

import neural.NeuralTest;

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
	public static void main(String[] args) throws IOException {
		if (args.length != 2) {
			System.err.println("Usage: test_type num_tests\ntest_type must be either neural, pid, or data");
			System.exit(1);
		}
		
		if (args[0].equals("neural")) {
			runNeuralTest(Integer.parseInt(args[1]));
		}
		if (args[0].equals("data")) {
			runDataTest(Integer.parseInt(args[1]));
		}
		//runPidTest();
		//runNeuralTest(5);
		//runDataTest(5);
	}
	
	private static void runNeuralTest(int numRuns) throws IOException {
		runTest(new NeuralTest(), numRuns);
	}
	
	private static void runDataTest(int numRuns) throws IOException {
		runTest(new DataCollector(), numRuns);
	}
	
	private static void runTest(Test tester, int numRuns) {
		try {
			tester.runTest(numRuns);
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

