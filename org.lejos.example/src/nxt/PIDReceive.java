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
import java.io.IOException;

import lejos.nxt.Button;
import lejos.nxt.ButtonListener;
import lejos.nxt.LCD;
import lejos.nxt.MotorPort;
import lejos.nxt.NXTMotor; //imports
import lejos.nxt.Sound;
import lejos.nxt.comm.USB;
import lejos.nxt.comm.USBConnection;
import lejos.util.Timer;
import lejos.util.TimerListener;

public class PIDReceive implements TimerListener{
	USBConnection usbCon;// a bluetooth connection
	DataInputStream dis;// data input and output sreams
	DataOutputStream dos;
	int command;
	boolean stopping = false;
	NXTMotor motA = new NXTMotor(MotorPort.A);
	NXTMotor motB = new NXTMotor(MotorPort.B);
	NXTMotor motC = new NXTMotor(MotorPort.C);
	
	//variables for the timer
	double speed = 0;
	long timePrev = System.currentTimeMillis();
	long timeNow = timePrev;
	long timeElapsed = 0;
	int degrees = 0;

	public static void main(String[] args) throws Exception {
		//rec rec = new rec();
		PIDReceive rec = new PIDReceive();
		rec.start();

		
	}

	public void start() {
		try {
			LCD.drawString("waiting", 0, 0);
			LCD.refresh();
			Button.ENTER.addButtonListener(new ButtonListener() {
				public void buttonPressed(Button b) {
					USB.cancelConnect();
					Sound.beep();
					stop();
				}

				@Override
				public void buttonReleased(Button b) {
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
			

			//NXTMotor curMot = motA;
			//int curPower=0;
			//char motChar = 'A';


			
			//set up the PID controller
			//final PIDController pidController = new PIDController(.1, 0, 0);
			//pidController.setInputRange(0,10); // The input limits
			//pidController.setOutputRange(-100,100); // The output limits
			//pidController.setSetpoint(1.2); // My target value (PID should minimize the error between the input and this value)
			//pidController.enable();
			//double input = 0;
			//double output = 0;
			


			
			Timer timer = new Timer(10, this);
			timer.start();
			
			while (true)// infinite loop to read in commands from the PC
			{
				if (stopping) stop();
				try {
					/*All controls should be performed computer side (send program)*/
					/*Need to calculate speed in a loop on brick*/
					/*Or rather, make a timer that calculates it every... 10 ms?*/
		            
		            //motA.setPower((int)output);
				} catch (Exception E) {
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

	@Override
	/*This is where speed is calculated*/
	/*Also send back through the stream*/
	/*And I believe the PID output should be set as the new power for motA*/
	public void timedOut() {
		degrees = motA.getTachoCount();
		motA.resetTachoCount();
		timeElapsed = timeNow - timePrev;
		speed = (double)degrees/timeElapsed*(double)1000/360;
		LCD.drawString(""+speed, 0, 7);
		try {
			dos.writeDouble(speed);
			dos.writeInt(motA.getPower());
			dos.writeInt(motB.getPower());
			dos.writeInt(motC.getTachoCount());
			dos.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		timePrev = timeNow;
		
	}

}


