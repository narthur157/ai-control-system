package nxt;

/*Copyright (c) 2011 Aravind Rao

 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation 
 * files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, 
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the 
 * Software is furnished to do so, subject to the following conditions:
 The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS 
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT 
 * OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

import java.io.DataInputStream;
import java.io.DataOutputStream;

import lejos.nxt.Button;
import lejos.nxt.ButtonListener;
import lejos.nxt.LCD;
import lejos.nxt.Motor;
import lejos.nxt.MotorPort;
import lejos.nxt.NXTMotor; //imports
import lejos.nxt.Sound;
import lejos.nxt.comm.USB;
import lejos.nxt.comm.USBConnection;
import lejos.util.Delay;
import lejos.util.Stopwatch;

public class rec {

	USBConnection usbCon;// a bluetooth connection
	DataInputStream dis;// data input and output sreams
	DataOutputStream dos;
	int command;
	boolean stopping = false;
	NXTMotor motA = new NXTMotor(MotorPort.A);
	NXTMotor motB = new NXTMotor(MotorPort.B);
	NXTMotor motC = new NXTMotor(MotorPort.C);
	Stopwatch timer = new Stopwatch();

	public static void main(String[] args) throws Exception {
		rec rec = new rec();
		rec.start();
	}
	// credit to Gregory Pakosz on SO for this: http://stackoverflow.com/questions/1936857/convert-integer-into-byte-array-java
	byte[] toBytes(int i)
	{
	  byte[] result = new byte[4];

	  result[0] = (byte) (i >> 24);
	  result[1] = (byte) (i >> 16);
	  result[2] = (byte) (i >> 8);
	  result[3] = (byte) (i /*>> 0*/);

	  return result;
	}
	// store a in the 2nd least significant byte, b in the first
	public int EncodeData(int a, int b) {
		int retVal = (a & 0xFF) << 8;
		retVal |= (b & 0xFF);
		return retVal;
	}
//	public byte[] EncodeData(int a, int b) {
//		byte[] aBytes = toBytes(a);
//		byte[] bBytes = toBytes(b);
//		byte[] combined = new byte[4];
//		combined[0] = aBytes[0];
//		combined[2] = bBytes[1];
//		combined[3] = 0;
//		combined[4] = 0;
//		return combined;
////		ByteBuffer buf = ByteBuffer.allocate(4);
////		a = (a & 0xFF) << 8;
////		a |= (b & 0xFF);
////		buf.putInt(a);
////		return b.array();
//	}
	public void start() {
		try {
			LCD.drawString("waiting", 0, 0);
			LCD.refresh();
			// If you're like me, you probably get annoyed at nonsense like
			// having to hard reset the nxt
			// if you start the programs in the wrong order. So if you press the
			// orange button,
			// the NXT will give up trying to do this stuff
			Button.ENTER.addButtonListener(new ButtonListener() {
				public void buttonPressed(Button b) {
					USB.cancelConnect();
					Sound.beep();
					stop();
				}

				@Override
				public void buttonReleased(Button b) {
					// interface requires this to be implemented. Classic java
					// being java
				}
			});

			usbCon = USB.waitForConnection();// wait until a connection is
												// opened with the PC
			LCD.clear();
			LCD.drawString("connected", 0, 0);
			LCD.refresh();
			dos = usbCon.openDataOutputStream();
			
			dis = usbCon.openDataInputStream(); // create the data input and
												// output streams
			//int iasdf = nSysTime; need to call C function here

			final int RIGHT=39, LEFT=37, UP=38, DOWN=40;
	//		int curA = 0, curB = 0, curC = 0, prevA = 0, prevB = 0, prevC = 0;
			Motor.A.suspendRegulation();
			NXTMotor curMot = motA;
			int curPower=0;
			char motChar = 'A';
			while (true)// infinite loop to read in commands from the PC
			{
				if (stopping) stop();
				//dos.writeInt(1);
				//dos.flush();
				try {
					// make this non-blocking
					// available only checks for bytes available, so it's possible that
					// we could block if we wrote something smaller than an int
					// but who cares
					//LCD.drawString("Max speed: "+Motor.A.getMaxSpeed(), 0, 4);
					LCD.drawString("A "+motA.getPower(), 0, 5);
					LCD.drawString(""+getSpeed(motA), 5, 5);
					LCD.drawString("B "+motB.getPower(), 0, 6);
					LCD.drawString("C "+motC.getPower(), 0, 7);
					LCD.refresh();
					if (dis.available() > 0) {
						
						command = dis.readInt();// read in command from PC
						if (command != -2) LCD.clear();
						
						if (command == -1) {
							stop();
						} else if (command == 0) { 
							// if command is 0, terminate connection and wait for a new one
							dis.close();
							dos.close();
							LCD.clear();
							usbCon.close();
							start();// start the process of reconnecting again
						} else {
							if (command == UP) {
								curMot.setPower(curMot.getPower()+10);
							} else if (command == DOWN) {
								curMot.setPower(curMot.getPower()-10);
							} else if (command == RIGHT) {
								if(curMot == motA) {
									curMot = motB;
									motChar='B';
								} else if(curMot == motB) {
									curMot = motC;
									motChar='C';
								} else if(curMot == motC) {
									curMot = motA;
									motChar='A';
									//dos.write(EncodeData(0, motA.getTachoCount()), 0, 4);
									LCD.drawString("trying to write",0,4);
									dos.writeInt(1);//(EncodeData(1, motA.getTachoCount()));
									dos.flush();
									LCD.drawString("wrote?",0,5);
								}
							} else if (command == LEFT) {
								if(curMot == motA) {
									curMot = motC;
									motChar='C';
								} else if(curMot == motB) {
									curMot = motA;
									motChar='A';
									//dos.write(EncodeData(0, motA.getTachoCount()), 0, 4);
									LCD.drawString("trying to write",0,4);
									dos.writeInt(2);        //EncodeData(1, motA.getTachoCount()));
									dos.flush();
									LCD.drawString("wrote?",0,5);
								} else if(curMot == motC) {
									curMot = motB;
									motChar='B';
								}
							} else {
								//motA.setPower(0);
							}
							LCD.drawString(""+motChar, 0, 3);
							if (command != -2) LCD.drawString(""+command, 0, 2);
							LCD.drawString(""+curMot.getPower(), 0, 1);
						}
						
					} else {
						
					}
					// more than likely, it makes more sense to only write these if the values actually changed
					// otherwise, who cares
					// by encoding which motor it is rather than serializing the process it doesn't matter which
					// order it is in anyhow
					
//					dos.writeInt(EncodeData(1, motB.getTachoCount()));
//					dos.flush();
//					dos.writeInt(EncodeData(2, motC.getTachoCount()));
//					dos.flush();
				} catch (Exception E) {
					// beeps are about as informative as a stack trace in my experience
					Sound.beep();
				}

			}
		} catch (Exception E) {
		}
	}

	public void stop()// stop method to shutdown the NXT
	{
		try {
			stopping=true;
			dis.close();// close all connections
			dos.close();
			Thread.sleep(100);
			LCD.clear();
			LCD.drawString("closing", 0, 0);
			LCD.refresh();
			usbCon.close();
			LCD.clear();
		}

		catch (Exception e) {
		}
	}
	
	double getSpeed(NXTMotor motX){
		motX.resetTachoCount();
		timer.reset();
		Delay.msDelay(50);
		return ((double)motX.getTachoCount() * 100.0 /timer.elapsed());
	}

}
