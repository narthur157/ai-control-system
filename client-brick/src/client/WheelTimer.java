package client;

import java.util.ArrayList;

import lejos.nxt.LCD;
import lejos.nxt.NXTMotor;
import lejos.util.Delay;
import lejos.util.Stopwatch;

public class WheelTimer extends Thread {
	Stopwatch speedTimer = new Stopwatch();
	Stopwatch executionTimer = new Stopwatch();
	NXTMotor mot;
	double speed = 0;
	double altSpeed = 0;
	ArrayList<Double> queue = new ArrayList<Double>();
	private final static int QUEUE_SIZE = 10;
	
	public WheelTimer(NXTMotor motInit) {
		mot = motInit;
	}
	
	// continuously update the value of speed rather than blocking and updating when called
	public void run() {
		while (!isInterrupted()) {
			executionTimer.reset();
			int count = mot.getTachoCount();
			mot.resetTachoCount();
			
			synchronized(this) {
				// multiply by 1000 to get degrees/second instead of degrees/millisecond
				queue.add(((double) count) * 1000.0 / (double) speedTimer.elapsed());
				altSpeed = ((double) count) * 1000.0 / (double) speedTimer.elapsed();
				if (queue.size() > QUEUE_SIZE) {
					queue.remove(0);
				}
				
				speed = 0;
				
				for (double d : queue) {
					speed += d;
				}
				
				speed /= queue.size();
			}
			
			speedTimer.reset();
			
			// would prefer a more accurate timer
			Delay.msDelay(10);
		}
	}
	
	// synchronized for thread safety
	synchronized double getSpeed() {
		LCD.drawString("speed: " + speed, 0, 6);
		LCD.drawString("altSpeed: " + altSpeed, 0, 1);
		return speed;
	}
	
	public void stopThread() {
		this.interrupt();
	}
}
