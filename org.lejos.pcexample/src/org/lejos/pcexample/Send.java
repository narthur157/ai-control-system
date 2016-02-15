package org.lejos.pcexample;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import lejos.pc.comm.NXTCommLogListener;
import lejos.pc.comm.NXTConnector;


//for use with Receive.java

public class Send {	
	public static void main(String[] args) throws IOException {
		NXTConnector conn = new NXTConnector();

		conn.addLogListener(new NXTCommLogListener(){

			public void logEvent(String message) {
				System.out.println("USBSend Log.listener: "+message);

			}

			public void logEvent(Throwable throwable) {
				System.out.println("USBSend Log.listener - stack trace: ");
				throwable.printStackTrace();

			}

		} 
				);

		if (!conn.connectTo("usb://")){
			System.err.println("No NXT found using USB");
			System.exit(1);
		}

		DataInputStream inDat = new DataInputStream(conn.getInputStream());
		DataOutputStream outDat = new DataOutputStream(conn.getOutputStream());
		final PrintWriter out = new PrintWriter(new FileWriter("/home/bu/PIDdata.txt"));


		//values for PID controller
		final double P = 0.83;		// proportional multiplier
		final double I = 0.28;		// integral multiplier
		final double D = 0.00; 		// derivative multiplier
		double currentSpeed = 0.0;  // process variable
		double maxOutput = 8.0;   	// limits to changes made in one cycle
		double minOutput = -8.0;   	
		double prevError = 0.0;   	// used to compute derivative component
		double totalError = 0.0; 	// used to compute integral component
		double tolerance = 4.0;  	// acceptable range of speed = desiredSpeed +/- tolerance
		double desiredSpeed = 32.0;	// set point
		double error = 0.0;			// used to compute proportional component
		double result = 0.0;		// amount to change driving wheel's power
		boolean stable = false;		// when stable, change disturbance and start again
		int cyclesStable = 0;		// keep track of how long speed stays in acceptable range
		int testStable = 10;			// number of loops speed must remain in range to move on

		//additional values to print each cycle (see line 127 for print statement)
		int time = 0;				//total elapsed time
		int currentPower = 0;		//power of the measured wheel
		int disturbPower = 0;		//power of the disturbance wheel
		int angle = 0;				//angle of the arm

		out.println("" + desiredSpeed + '\t' + P + '\t' + I + '\t' + D);

		int numLoops = 1000;
		for(int i=0;i<numLoops;++i) 
		{

			//sends a power level for the disturbance wheel within +/-50% of desired speed
			//note: try just sending a set nonsense int (ie 101) and gen rand # in brick
			//update: sends 101 to signal brick to change the disturbance wheel power
			try {
				outDat.writeInt(101);
				outDat.flush();
			} catch (IOException ioe) {
				System.err.println("IO Exception writing bytes");
			}

			//reads the new disturbance power level generated in the brick
			try {
				disturbPower = inDat.readInt();
				out.println("d\t" + disturbPower);
			} catch (IOException ioe) {
				System.err.println("IO Exception reading reply");
			}     

			//PID control loop
			stable = false;
			while(!stable){
				//calculate control value
				error = desiredSpeed - currentSpeed;
				totalError += error;
				//if(totalError * I > maxOutput) totalError = maxOutput/I;
				//else if(totalError * I < minOutput) totalError = minOutput/I;
				result = P * error + I * totalError + D * (error - prevError);
				prevError = error;
				
				// clamp result between max and min
				if (result > maxOutput) { result = maxOutput; }
				else if (result < minOutput) { result = minOutput; }
				
				if (currentSpeed >= desiredSpeed - tolerance && 
					currentSpeed <= desiredSpeed + tolerance) { cyclesStable++; }
				else { cyclesStable = 0; }
				
				if (cyclesStable >= testStable){
					stable = true;
					cyclesStable = 0;
				}
				
				outDat.writeInt((int)result);
				outDat.flush();

				//read values back from brick
				try {
					time = inDat.readInt();
					currentSpeed = inDat.readDouble();
					currentPower = inDat.readInt();
					angle = inDat.readInt();	
				} catch (IOException ioe) {
					System.err.println("IO Exception reading reply");
				}     

				//print values to file
				out.println("" + time +  '\t' + currentSpeed + '\t' + currentPower + '\t' + angle);
				System.out.println(result);
			}
		}




		try {
			inDat.close();
			outDat.close();
			System.out.println("Closed data streams");
		} catch (IOException ioe) {
			System.err.println("IO Exception Closing connection");
		}

		try {
			conn.close();
			System.out.println("Closed connection");
		} catch (IOException ioe) {
			System.err.println("IO Exception Closing connection");
		}
		
		out.close();
	}
}