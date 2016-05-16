package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import lejos.nxt.Button;
import lejos.nxt.ButtonListener;
import lejos.nxt.LCD;
import lejos.nxt.Sound;
import lejos.nxt.comm.USB;
import lejos.nxt.comm.USBConnection;

public class PCComm {
	USBConnection conn;
	DataInputStream dIn;
	DataOutputStream dOut;
	
	public PCComm() {
        Button.ENTER.addButtonListener(new ButtonListener() {
            public void buttonPressed(Button b) {
                    USB.cancelConnect();
                    Sound.beep();
                    try {
						close();
					} catch (IOException e) {
						e.printStackTrace();
					}
            }

            @Override
            public void buttonReleased(Button b) {
                    // interface requires this to be implemented. Classic java
                    // being java
            }
        });

		LCD.drawString("waiting", 0, 0);
		conn = USB.waitForConnection();
		LCD.clear();
		LCD.drawString("connected", 0, 0);
		LCD.refresh();
		dOut = conn.openDataOutputStream();
		dIn = conn.openDataInputStream();
	}

	public int receiveInt() throws IOException {
		return dIn.readInt();
	}
	
	public Command receiveCommand() throws IOException {
		byte[] bytes = new byte[2];
		dIn.read(bytes, 0, bytes.length);
		return new Command(bytes);
	}
	
	// could modify this to use far less bandwidth, but might not matter at all..
	// should do a test at some point, just send far more data than necessary and
	// see the effect on latency
	public void sendBrick(BrickState bs) throws IOException {
		dOut.writeInt(bs.time);
		dOut.writeInt((int) bs.disturbSpeed);
		dOut.writeInt(-1*bs.disturbPower);
		dOut.writeInt(bs.controlPower);
		dOut.writeInt(bs.torquePower);
		dOut.writeInt(bs.angle);
		dOut.flush();
	}
	
	public void sendInt(int i) throws IOException {
		dOut.writeInt(i);
		dOut.flush();
	}
	
	public void close() throws IOException {
		dOut.close();
		dIn.close();
		conn.close();
	}
}
