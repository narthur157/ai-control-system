package client;

import lejos.nxt.NXTMotor;
import lejos.util.Delay;
import lejos.util.Stopwatch;

public class WheelTimer {
	Stopwatch speedTimer = new Stopwatch();
	NXTMotor mot;
	
	public WheelTimer(NXTMotor motInit) {
		mot = motInit;
	}
	
	// might be better to have a thread which wakes up every 100ms or so
	// and calculates and sets the speed value, which would be accessible
	// via a getter. The speed could also be logged to a file this way 
	
	//returns speed of given motor as a double
	//this formula gives a value that is slightly lower than the
	//motor power if undamped (thus upper bounded by 100)
	//this also depends on the return value multiplier
	double getSpeed() {
		mot.resetTachoCount();
		speedTimer.reset();
		Delay.msDelay(160);
		//I've played around with the delay to try to find an optimal value
		//the speed is inaccurate at small values due to small tacho count
		//but we don't want it to get too big or cycle time is long. Also note 
		//that certain speeds/differences between wheels can cause the machine to 
		//"jump" around and grind the wheels, which causes even more innacuracies.
		
		return ((double) mot.getTachoCount() * 120.0 /speedTimer.elapsed());
		//the multiplier here is just to get a comfortable range of speed values
	}
}
