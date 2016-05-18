package server;

import java.io.IOException;

import neural.NeuralController;
import pid.PIDController;
import testing.ComparisonTest;
import testing.ControllerDisturbanceTest;
import testing.DataGeneration;
import testing.Test;

import communication.BrickComm;

/**
 * The driver for the server side code
 * Runs tests based on command line argument, as specified in the run script
 * 
 * @see communication.BrickComm
 * @author Nick Arthur
 */
public class Send {	
	public static void main(String[] args) throws IOException {
		if (args.length != 2) {
			System.err.println("Usage: test_type num_tests\ntest_type must be either neural, pid, or data");
			System.exit(1);
		}
		
		System.out.println("Doing test " + args[0]);
		
		if (args[0].equals("neural")) {
			runNeuralTest(Integer.parseInt(args[1]));
		}
		if (args[0].equals("data")) {
			runDataTest(Integer.parseInt(args[1]));
		}
		if (args[0].equals("pid")) {
			runPIDTest(Integer.parseInt(args[1]));
		}
		if (args[0].equals("benchmark")) {
			runBenchmarkTest(Integer.parseInt(args[1]));
		}
		//runPidTest();
		//runNeuralTest(5);
		//runDataTest(5);
	}
	
	private static void runBenchmarkTest(int numRuns) throws IOException {
		runTest(new ComparisonTest(), numRuns*2);
	}

	private static void runNeuralTest(int numRuns) throws IOException {
		runTest(new ControllerDisturbanceTest(new NeuralController()), numRuns);
	}
	
	private static void runDataTest(int numRuns) throws IOException {
		runTest(new DataGeneration(), numRuns);
	}
	
	private static void runPIDTest(int numRuns) throws IOException {
		runTest(new ControllerDisturbanceTest(new PIDController()), numRuns);
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

