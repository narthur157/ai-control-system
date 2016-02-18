package tests;

import java.io.IOException;
import server.BrickComm;
import server.BrickState;
import server.Logger;

public abstract class Test {
	protected BrickComm comm;
	protected Logger logger;
	
	public Test(BrickComm commInit) {
		comm = commInit;
	}
	
	public void runTest(int numLoops) throws IOException {
		try {
			for (int i=0;i<numLoops;++i) {
				test();
				collectData();
			}
		}
		finally {
			logger.close();
		}
	}
	
	abstract public void test() throws IOException;

	public void collectData() throws IOException {
		try {
			BrickState bs = comm.readBrick();
			logger.logln(bs.toString());
			System.out.println(bs.toString());
		}
		catch(IOException ioe) {
			logger.close();
			throw ioe;
		}
	}
}
