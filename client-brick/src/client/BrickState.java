// this file should match the one on the server
package client;

public class BrickState {
	public int time, controlPower, disturbPower, torquePower, angle;
	public double disturbSpeed;
	
	public BrickState(int time, int currentSpeed, int disturbPower, 
					  int currentPower,int torquePower, int angle) {
		this.time = time; 					//total elapsed time
		this.disturbSpeed = currentSpeed;
		this.disturbPower = disturbPower;
		this.controlPower = currentPower; 	//power of the measured wheel
		this.torquePower = torquePower;
		this.angle = angle; 				//angle of the arm
	}
	
	public String toString() {
		return "" + time +  '\t' + disturbSpeed + '\t' + disturbPower + 
				'\t' + torquePower + '\t' + controlPower + '\t' + angle;
	}

}
