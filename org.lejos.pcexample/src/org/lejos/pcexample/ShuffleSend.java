package org.lejos.pcexample;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import lejos.pc.comm.NXTCommLogListener;
import lejos.pc.comm.NXTConnector;
 
public class ShuffleSend {	
	public static void main(String[] args) {
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
		
		ArrayList<Integer> powers = new ArrayList<Integer>();
		for(int i = 0; i < 121; i++){
			powers.add(new Integer(i));
		}
		Collections.shuffle(powers);
		
		double curSpeed = 0;
		for(int i = 0; i < 121; i++){
			
			try{
				//outDat.writeInt(-powers.get(i)/11*10);
				//outDat.writeInt(powers.get(i)%11*10);
				outDat.writeInt(powers.get(i));
				outDat.flush();
			} catch(IOException ioe){
				System.err.println("IO Exception writing bytes");
			}
			
			try{
				curSpeed = inDat.readInt();
			} catch(IOException ioe){
				System.err.println("IO Exception reading reply");
			}
			System.out.println(curSpeed);
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
	}
}