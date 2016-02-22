package tests;

import java.io.IOException;
import server.BrickComm;
import server.BrickState;
import server.Logger;

public abstract class Test {
	protected BrickComm comm;
	protected Logger logger;
	protected int changeFlag = 0;
	protected BrickState bs;
	protected BrickState prevBs;
	
	public Test(BrickComm commInit) {
		comm = commInit;
	}
	
	public void runTest(int numLoops) throws IOException {
		try {
			for (int i=0;i<numLoops;++i) {
				// don't change anything until the system stabilizes
				if (bs == prevBs) {
					test();
				}
				else {
					if (bs != null && prevBs != null) 
						System.out.println("bs: " + bs.toString() + " != prevBs: "  + prevBs.toString());
				}
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
			prevBs = bs;
			bs = comm.readBrick();
			logger.logln(bs.toString() + "\t" + changeFlag);
			System.out.println(bs.toString());
		}
		catch(IOException ioe) {
			logger.close();
			throw ioe;
		}
	}
}
