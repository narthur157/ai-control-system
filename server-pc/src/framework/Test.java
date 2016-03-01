package framework;

import java.io.IOException;

import communication.BrickComm;
import communication.BrickListener;

public abstract class Test implements BrickListener {
	private final int STABLE_COUNT = 200;
	
	protected BrickComm comm;
	protected Logger logger;
	protected int changeFlag = 0;
	protected BrickState bs, prevBs;
	
	private int testCount = 0, numLoops = 0, stableCount = 0;
	
	public Test(BrickComm commInit) throws IOException {
		comm = commInit;
		logger = new FileLogger();
	}
	
	/**
	 * 
	 * @param commInit
	 * @param loggerInit - Logger written to in collectData
	 * @throws IOException
	 */
	public Test(BrickComm commInit, Logger loggerInit) throws IOException {
		comm = commInit;
		logger = loggerInit;
	}
	
	/**
	 * Do anything that should change between tests
	 * Gets called once the system has stabilized
	 * @throws IOException
	 */
	abstract protected void test() throws IOException;
	
	/**
	 * 
	 * @param numLoopsIn
	 * @throws IOException
	 */
	final public void runTest(int numLoopsIn) throws IOException {
		numLoops = numLoopsIn;
		
		try {
			comm.addListener(this);
			
			while(testCount < numLoops) {
				// I don't think this needs to be synchronized because
				// we know this will only be notified when the comparison fails
				synchronized(this) {
					this.wait();
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		finally {
			logger.close();
		}
	}
		
	/** 
	 * this signifies that updateBrick has been called,
	 * but a new test has not necessarily started
	*/ 
	protected void update() { /* do nothing unless overridden */ }
	
	/**
	 * Implement BrickListener - receive a state update
	 * from a brick
	 */
	final public void updateBrick(BrickState bsIn) {
		prevBs = bs;
		bs = bsIn;
		collectData();
		
		update();
		
		// only run updates once stabilized
		if (!bs.equals(prevBs)) {
			//System.out.println("Resetting stable count. Count was " + stableCount);
			if (bs != null && prevBs != null)
				//System.out.println("bs: " + bs.toString() + " prevBs: " + prevBs.toString());
			stableCount = 0;
		}
		else {
			stableCount++;
			if (stableCount > STABLE_COUNT) {
				stableCount = 0;
				
				try {
					
					test();
					testCount++;
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (testCount >= numLoops) {
					System.out.println("Completed " + testCount + " tests");
					synchronized(this) {
						this.notify();
					}
				}
			}
		}
	}
	
	final private void collectData() {
		logger.logln(bs.toString() + "\t" + changeFlag);
	}
}
