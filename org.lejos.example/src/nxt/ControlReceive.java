package nxt;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.lang.System;

import lejos.nxt.Button;
import lejos.nxt.ButtonListener;
import lejos.nxt.LCD;
import lejos.nxt.MotorPort;
import lejos.nxt.NXTMotor;
import lejos.nxt.Sound;
import lejos.nxt.comm.USB;
import lejos.nxt.comm.USBConnection;

public class ControlReceive {
	USBConnection usbCon;
	DataInputStream dis;
	DataOutputStream dos;
	int command;
	boolean stopping = false;
	NXTMotor motA = new NXTMotor(MotorPort.A);
	NXTMotor motB = new NXTMotor(MotorPort.B);
	NXTMotor motC = new NXTMotor(MotorPort.C);
	final double TOLERANCE = 1;
	
	public static void main(String[] args){
		ControlReceive rec = new ControlReceive();
		rec.start();
	}
	
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
			

			final int RIGHT=39, LEFT=37, UP=38, DOWN=40;
	//		int curA = 0, curB = 0, curC = 0, prevA = 0, prevB = 0, prevC = 0;
			NXTMotor curMot = motA;
			int curPower=0;
			double desiredSpeedA = 8;
			double desiredSpeedB = 0;
			double desiredSpeedC = 0;
			char motChar = 'A';
			double smallTime = 0;
			double speed = 0;
			curMot.setPower(50);
			long timeStart = java.lang.System.currentTimeMillis();
			long timePrev = java.lang.System.currentTimeMillis();
			long timeElapsed = 0;
			
			while(true)
			{
				try
				{
					long timeNow = java.lang.System.currentTimeMillis();
					int degrees = motA.getTachoCount();
					if(degrees<30)continue;
					motA.resetTachoCount();
					LCD.drawString(""+timeNow,0,4);
					LCD.drawString(""+timeElapsed,0,5);
					LCD.drawString(""+degrees, 0, 6);
					timeElapsed = timeNow - timePrev;
					speed = (double)degrees/timeElapsed*(double)1000/360;
					LCD.drawString(""+speed, 0, 7);
					dos.writeDouble(speed);
					dos.flush();
					//displays last calculated speed.
					//if the motor is turning slowly or backwards 
					//(<30 degrees in a given time period)
					//speed is not recalculated.

					LCD.drawString(""+motA.getPower(),0,3);
					//LCD.refresh();
					
					timePrev=timeNow;
					/*
					motA.resetTachoCount();
					LCD.drawString(""+speed, 0, 1);
					if(speed < desiredSpeedA - TOLERANCE && motA.getPower()<99){
						motA.setPower(motA.getPower()+1);
						LCD.drawString("pow++", 0, 2);
					}
					else if(speed > desiredSpeedA + TOLERANCE && motA.getPower()>-99){
						motA.setPower(motA.getPower()-1);
						LCD.drawString("pow--", 0, 2);
					}
					else LCD.drawString("pow==", 0, 2);*/
				}
				catch(Exception E){}
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

}



