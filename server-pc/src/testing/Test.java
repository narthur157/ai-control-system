package testing;

import java.io.IOException;

import communication.BrickComm;
import communication.BrickListener;
import framework.BrickState;
import framework.FileLogger;
import framework.Logger;


/**
 * This abstract class requires the extension of the 'test' method
 * which gets called when the system has stabilized for the amount of
 * time specified by testLength (milliseconds). Brick updates come about
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
	
	protected int curTestCount = 0;
	protected int numTests = 0;
	
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
	 * Gets called every testLength milliseconds
	 * @throws IOException
	 */
	abstract protected void test();
	
	/**
	 * 
	 * @param numLoopsIn
	 * @throws IOException
	 */
	final public void runTest(int numLoopsIn) throws IOException {
		numTests = numLoopsIn;
		
		try {
			BrickComm.addListener(this);
			
			while(curTestCount <= numTests) {
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
		
		logData();

		long curTime = System.currentTimeMillis();
		
		if (curTime - prevTime > testLength || prevTime == -1) {
			curTestCount++;
			
			if (curTestCount > numTests) {
				finishTest();
			}
			
			prevTime = curTime;
			System.out.print("Test " + curTestCount + " out of " + numTests + "\r");
			test();
		}
	}
	
	final protected void finishTest() {
		System.out.println("Completed " + (curTestCount-1) + " tests");
		
		synchronized(this) {
			// wake up the thread in runTest
			this.notify();
		}
	}
	
	final private void logData() {
		String s = collectData();
		
		if (!s.equals("")) {
			logger.logln(collectData());
		}
	}
	
	/**
	 * This method can be decorated by subclasses
	 * by overriding and doing `return super.collectData + "decoration"` 
	 * in subclass
	 * @see ControllerDisturbanceTest
	 * @return
	 */
	protected String collectData() {
		// logger is paramterized in constructor, can write to file
		return bs.toString();
	}	
}
