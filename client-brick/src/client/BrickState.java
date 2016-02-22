// this file should match the one on the server
package client;

public class BrickState {
	public int time, controlPower, disturbPower, angle;
	public double disturbSpeed;
	
	public BrickState(int time, double currentSpeed, int currentPower, int disturbPower, int angle) {
		this.time = time; 					//total elapsed time
		this.disturbSpeed = currentSpeed;
		this.controlPower = currentPower; 	//power of the measured wheel
		this.disturbPower = disturbPower;
		this.angle = angle; 				//angle of the arm
	}
	
	public String toString() {
		return "" + time +  '\t' + disturbSpeed + '\t' + disturbPower + '\t' + controlPower + '\t' + angle;
	}

}
