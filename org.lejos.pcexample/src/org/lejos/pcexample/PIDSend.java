package org.lejos.pcexample;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import org.lejos.pcexample.PIDController;

import lejos.pc.comm.NXTConnector;

public class PIDSend {
	
	DataOutputStream dos;
	static DataInputStream dis;
	static boolean disconnected = true;

	public static void main(String[] args){
		
		send send = new send();
		send.connect();
		
		//set up the PID controller
		final PIDController pidController = new PIDController(.1, 0, 0);
		pidController.setInputRange(-99,99); // The input limits
		pidController.setOutputRange(-99,99); // The output limits
		pidController.setSetpoint(8); // My target value (PID should minimize the error between the input and this value)
		pidController.enable();
		double input = 0;
		double output = 0;
		
	}

	
	public void connect() {

		NXTConnector conn = new NXTConnector();// create a new NXT connector
		boolean connected = conn.connectTo("usb://"); // try to connect to any
														// NXT over usb

		if (!connected) {// failure
			System.out.println("Failed to connect to any NXT\n");
			System.out.println("Press Reconect to retry.\n");
		}

		else {
			disconnected=false;
			System.out.println("Connected to " + conn.getNXTInfo() + "\n");
			dos = new DataOutputStream(conn.getOutputStream());
			dis = new DataInputStream(conn.getInputStream());

		}

	}
}
