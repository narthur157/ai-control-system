// this file should match the one on the server
package client;

public class BrickState {
	public int time, controlPower, disturbPower, torquePower, angle;
	public double disturbSpeed;
	
	public BrickState(int time, int currentSpeed, int currentPower, 
					  int disturbPower, int torquePower, int angle) {
		this.time = time; 					//total elapsed time
		this.disturbSpeed = currentSpeed;
		this.controlPower = currentPower; 	//power of the measured wheel
		this.disturbPower = disturbPower;
		this.torquePower = torquePower;
		this.angle = angle; 				//angle of the arm
	}
	
	public String toString() {
		return "" + time +  '\t' + disturbSpeed + '\t' + disturbPower + 
				'\t' + torquePower + '\t' + controlPower + '\t' + angle;
	}

}
