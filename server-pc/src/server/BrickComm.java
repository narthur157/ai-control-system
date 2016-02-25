package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import lejos.pc.comm.NXTCommLogListener;
import lejos.pc.comm.NXTConnector;

public class BrickComm extends Thread {
	private DataInputStream inDat;
	private DataOutputStream outDat;
	private NXTConnector conn;
	private ArrayList<BrickListener> listeners = new ArrayList<BrickListener>();
	
	// asynchronously receive updates from the brick
	public void run() {
		while (true) {
			try {
				// blocking call
				BrickState bs = readBrick();
				for (BrickListener l : listeners) {
					l.updateBrick(bs);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// Listener pattern 
	public void addListener(BrickListener bl) {
		listeners.add(bl);
	}
	
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
	
	private BrickState readBrick() throws IOException {
		//read values back from brick
		try {
			// the order matters here - it's the order they're sent in
			// this could all be reduced to about 8 bytes total....
			// but premature optimization and all right
			int time = inDat.readInt();
			double disturbSpeed = inDat.readDouble();
			int disturbPower = inDat.readInt();
			int controlPower = inDat.readInt();
			int angle = inDat.readInt();	
			return new BrickState(time, disturbSpeed, disturbPower, controlPower, angle);
		} catch (IOException ioe) {
			System.err.println("IO Exception reading reply");
			throw ioe;
		}
	}
	
	public void sendCommand(Command c) {
		try {
			outDat.write(c.bytes, 0, c.bytes.length);
			System.out.println("Sending " + c);
			outDat.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendCommand(byte wheel, int power) {
		sendCommand(new Command(wheel, (byte) power));
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
