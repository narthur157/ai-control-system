package org.lejos.pcexample;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;

import lejos.pc.comm.NXTCommLogListener;
import lejos.pc.comm.NXTConnector;

public class PIDSend2 {
	public static void main(String[] args) throws IOException {
		//set up the PID controller
		final PIDController pidController = new PIDController(.1, .01, 0);
		pidController.setInputRange(-10,10); // The input limits
		pidController.setOutputRange(-99,99); // The output limits
		pidController.setSetpoint(1.5); // My target value (PID should minimize the error between the input and this value)
		pidController.enable();
		//double input = 0;
		//double output = 0;
		
		PrintWriter out = new PrintWriter(new FileWriter("/home/bu/PIDdata.txt")); 
		
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
		
		//replace this with infinite while loop (or set for amount of times to run)
		//outDat.write(pidController.performPID());
		//x = inDat.readDouble();
		//pidController.getInput(x);
		
		
		/*int x = 0;
		for(int i=0;i<100;i++) 
		{
			try {
			   outDat.writeInt(i);
			   outDat.flush();
	
			} catch (IOException ioe) {
				System.err.println("IO Exception writing bytes");
			}
	        
			try {
	        	 x = inDat.readInt();
	        } catch (IOException ioe) {
	           System.err.println("IO Exception reading reply");
	        }            
	        System.out.println("Sent " +i + " Received " + x);
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
		}*/
		
		ArrayList<Integer> powers = new ArrayList<Integer>();
		for(int i = 0; i < 121; i++){
			int a = i/11*10;
			int b = i%11*10;
			if(a-b<50 && b-a < 50)
				powers.add(new Integer(i));
		}
		Collections.shuffle(powers);
		
		for(Integer pow: powers){
			
		}
		//TODO: problem: how to send "disturbance" powers and also PID powers through the same stream?
		//disturbance must be read as a power for a and a power for b,
		//while pid must be read as only a power for a...
		
		out.close();
	}
}
