package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import lejos.pc.comm.NXTCommLogListener;
import lejos.pc.comm.NXTConnector;

public class BrickComm {
	private DataInputStream inDat;
	private DataOutputStream outDat;
	private NXTConnector conn;
	
	public BrickComm() {
		conn = new NXTConnector();

		conn.addLogListener(new NXTCommLogListener() {
			public void logEvent(String message) {
				System.out.println("USBSend Log.listener: "+message);

			}

			public void logEvent(Throwable throwable) {
				System.out.println("USBSend Log.listener - stack trace: ");
				throwable.printStackTrace();

			}
		});
		
		if (!conn.connectTo("usb://")){
			System.err.println("No NXT found using USB");
			System.exit(1);
		}
		
		inDat = new DataInputStream(conn.getInputStream());
		outDat = new DataOutputStream(conn.getOutputStream());
		System.out.println("Successfully connected");
	}
	
	public BrickState readBrick() throws IOException {
		//read values back from brick
		try {
			int time = inDat.readInt();
			double currentSpeed = inDat.readDouble();
			int currentPower = inDat.readInt();
			int angle = inDat.readInt();	
			return new BrickState(time, currentSpeed, currentPower, angle);
		} catch (IOException ioe) {
			System.err.println("IO Exception reading reply");
			throw ioe;
		}
	}
	
	public void sendCommand(Command c) {
		try {
			outDat.write(c.bytes, 0, c.bytes.length);
			System.out.println("Sending " + c);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendInt(int i) {
		try {
			outDat.writeInt(i);
			outDat.flush();
		} catch (IOException ioe) {
			System.err.println("IO Exception writing bytes");
		}
	}
	
	public int receiveInt() throws IOException {
		try {
			return inDat.readInt();
		} catch (IOException ioe) {
			System.err.println("IO Exception reading reply");
			throw ioe;
		} 
	}
	
	public void close() {
		try {
			inDat.close();
			outDat.close();
			System.out.println("Closed data streams");
		} catch (IOException ioe) {
			System.err.println("IO Exception Closing connection");
		}
	}
}
