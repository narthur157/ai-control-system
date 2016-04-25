package framework;

import java.io.IOException;

import communication.BrickComm;
import communication.BrickListener;


/**
 * This abstract class requires the extension of the 'test' method
 * which gets called when the system has stabilized for the amount of
 * brick updates indicated by STABLE_COUNT. Brick updates come about
 * every 10ms with some variation. 
 * 
 * runTest is the only method users of the class should use, as updateBrick
 * is intended only for the BrickUpdater. 
 * 
 * @author Nicholas Arthur
 *
 */
public abstract class Test implements BrickListener {
	//private final int STABLE_COUNT = 200;
	
	protected Logger logger;
	protected int changeFlag = 0;
	protected long testLength = 2000;
	protected BrickState bs, prevBs;
	
	private long prevTime = -1;
	
	private int testCount = 0, numLoops = 0, stableCount = 0;
	
	public Test() throws IOException {
		logger = new FileLogger();
	}
	
	/**
	 * 
	 * @param loggerInit - Logger written to in collectData
	 * @throws IOException
	 */
	public Test(Logger loggerInit) throws IOException {
		logger = loggerInit;
	}
	
	/**
	 * Do anything that should change between tests
	 * Gets called once the system has stabilized
	 * @throws IOException
	 */
	abstract protected void test();
	
	/**
	 * 
	 * @param numLoopsIn
	 * @throws IOException
	 */
	final public void runTest(int numLoopsIn) throws IOException {
		numLoops = numLoopsIn;
		
		try {
			BrickComm.addListener(this);
			
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
			BrickComm.rmListener(this);
			logger.close();
		}
	}
	
	/**
	 * Implement BrickListener - receive a state update
	 * from a brick
	 */
	final public void updateBrick(BrickState bsIn) {
		prevBs = bs;
		bs = bsIn;
		collectData();
//
//		// only run updates once stabilized
//		if (!bs.equals(prevBs)) {
//			//System.out.println("Resetting stable count. Count was " + stableCount);
//			//if (bs != null && prevBs != null)
//				//System.out.println("bs: " + bs.toString() + " prevBs: " + prevBs.toString());
//			stableCount = 0;
//		}
//		else {
			long curTime = System.currentTimeMillis();
			if (curTime - prevTime > testLength || prevTime == -1) {
				prevTime = curTime;
				test();
				testCount++;
				
				if (testCount >= numLoops) {
					finishTest();
				}
			}
//			stableCount++;
//			if (stableCount > STABLE_COUNT) {
//				stableCount = 0;
//				
//				
//			}
//		}
	}
	
	final private void finishTest() {
		System.out.println("Completed " + testCount + " tests");
		
		synchronized(this) {
			// wake up the thread in runTest
			this.notify();
		}
	}
	
	final private void collectData() {
		//System.out.println(bs.toString());
		// logger is paramterized in constructor, can write to file
		logger.logln(bs.toString() + "\t" + changeFlag);
	}
}
