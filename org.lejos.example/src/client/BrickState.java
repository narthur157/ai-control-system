// this file should match the one on the server
package client;

public class BrickState {
	public int time, currentPower, angle;
	public double currentSpeed;
	
	public BrickState(int time, double currentSpeed, int currentPower, int angle) {
		this.time = time; 					//total elapsed time
		this.currentSpeed = currentSpeed;
		this.currentPower = currentPower; 	//power of the measured wheel
		this.angle = angle; 				//angle of the arm
	}
	
	public String toString() {
		return "" + time +  '\t' + currentSpeed + '\t' + currentPower + '\t' + angle;
	}

}
