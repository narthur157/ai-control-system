package client;

import lejos.nxt.NXTMotor;
import lejos.util.Delay;
import lejos.util.Stopwatch;

public class WheelTimer extends Thread {
	Stopwatch speedTimer = new Stopwatch();
	NXTMotor mot;
	double speed = 0;
	
	public WheelTimer(NXTMotor motInit) {
		mot = motInit;
	}
	
	// continuously update the value of speed rather than blocking and updating when called
	public void run() {
		while (true) {
			// multiply by 1000 to get degrees/second instead of degrees/millisecond
			// fancy synchronized block
			synchronized(this) {
				speed = (double) mot.getTachoCount() * 1000.0 /speedTimer.elapsed();
			}
			//I've played around with the delay to try to find an optimal value
			//the speed is inaccurate at small values due to small tacho count
			//but we don't want it to get too big or cycle time is long. Also note 
			//that certain speeds/differences between wheels can cause the machine to 
			//"jump" around and grind the wheels, which causes even more innacuracies.
			mot.resetTachoCount();
			speedTimer.reset();
			// 10 Hz update
			Delay.msDelay(100);
		}
	}
	
	// synchronized for thread safety
	synchronized double getSpeed() {
		return speed;
	}
}
