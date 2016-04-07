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
		while (!isInterrupted()) {
			int count = mot.getTachoCount();
			mot.resetTachoCount();
			
			synchronized(this) {
				// multiply by 1000 to get degrees/second instead of degrees/millisecond
				speed = (double) count * 1000.0 /speedTimer.elapsed();
			}
			
			speedTimer.reset();
			// 10 Hz update
			Delay.msDelay(10);
		}
	}
	
	// synchronized for thread safety
	synchronized double getSpeed() {
		return speed;
	}
	
	public void stopThread() {
		this.interrupt();
	}
}
