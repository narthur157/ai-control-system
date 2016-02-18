package tests;

import java.io.IOException;
import java.util.Random;

import server.BrickComm;
import server.BrickState;
import server.Command;
import server.Logger;

public abstract class Test {
	protected BrickComm comm;
	protected Logger logger;
	
	public Test(BrickComm commInit) {
		comm = commInit;
	}
	
	public void runTest(int numLoops) throws IOException {
		for (int i=0;i<numLoops;++i) {
			test();
			collectData();
		}	
	}
	
	abstract public void test() throws IOException;

	public void collectData() throws IOException {
		BrickState bs = comm.readBrick();
		logger.logln(bs.toString());
	}
}
