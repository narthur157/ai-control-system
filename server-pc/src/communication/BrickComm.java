package communication;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import lejos.pc.comm.NXTCommLogListener;
import lejos.pc.comm.NXTConnector;

/**
 * This class manages everything in the communication package
 * 
 * @author Nick Arthur
 */
public class BrickComm {
	private static DataInputStream inDat;
	private static DataOutputStream outDat;
	private static NXTConnector conn;
	private static BrickUpdater updater;
	private static boolean sending;
	
	// initialization
	static {
		start();
	}
	
	/**
	 * Connect to a brick, set up streams and the brick
	 * update event source
	 */
	public static void start() {
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
		
		updater = new BrickUpdater(inDat);
		updater.start();
		
		System.out.println("Successfully connected");
	}

	// Listener/Observer pattern
	public static void addListener(BrickListener bl) {
		updater.listeners.add(bl);
	}
	
	public static void rmListener(BrickListener bl) {
		updater.listeners.remove(bl);
	}
	
	/**
	 * Send the bit representation of a command
	 * @param c
	 */
	public static void sendCommand(Command c) {
		try {
			if (sending) {
				throw new IOException("Tried to interrupt command write");
			}
			//System.out.println("Sending " + c);
			sending = true;
			outDat.write(c.bytes, 0, c.bytes.length);
			outDat.flush();
			sending = false;
		} catch (IOException e) {
			
			e.printStackTrace();
			close();
		}
	}
	
	/**
	 * Convenience method to construct a Command and send it
	 * 
	 * @param wheel - Should be a wheel constant from Command
	 * @param power - Power for the motor, should be in [-100, 100]
	 */
	public static void sendCommand(byte wheel, int power) {
		sendCommand(new Command(wheel, (byte) power));
	}
	
	/**
	 * Stop all motors and execution on brick
	 */
	public static void stopBrick() {
		sendCommand(Command.STOP, 0);
	}
	
	/**
	 * Should undo everything from start
	 */
	public static void close() {
		updater.stopUpdater();
		
		try {
			inDat.close();
			outDat.close();
			System.out.println("Closed data streams");
			System.exit(0);
		} catch (IOException ioe) {
			System.err.println("IO Exception Closing connection");
			System.exit(1);
		}
	}
}
