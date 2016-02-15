package nxt;

//for use with Send.java

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
import java.io.EOFException;
import java.util.Random;

import lejos.nxt.LCD;
import lejos.nxt.MotorPort;
import lejos.nxt.NXTMotor; //imports
import lejos.nxt.comm.USB;
import lejos.nxt.comm.USBConnection;
import lejos.util.Delay;
import lejos.util.Stopwatch;

public class Receive{
	USBConnection usbCon;// a bluetooth connection
	DataInputStream dis;// data input and output sreams
	DataOutputStream dos;
	int command;
	boolean stopping = false;
	NXTMotor motA = new NXTMotor(MotorPort.A);	//use port A for the wheel we want to control
	NXTMotor motB = new NXTMotor(MotorPort.B);	//use port B for the disturbance wheel
	NXTMotor motC = new NXTMotor(MotorPort.C);	//use port C for the arm angle
	Stopwatch spTimer = new Stopwatch();		//timer used for calculating SPeed
	Stopwatch whTimer = new Stopwatch();		//times the WHole process


	public static void main(String[] args) throws Exception {
		Receive rec = new Receive();
		rec.start();
	}

	public void start(){
		LCD.drawString("waiting", 0, 0);
		USBConnection conn = USB.waitForConnection();
		LCD.clear();
		LCD.drawString("connected", 0, 0);
		LCD.refresh();
		DataOutputStream dOut = conn.openDataOutputStream();
		DataInputStream dIn = conn.openDataInputStream();

		int disturbPower = 0;	//variable for the power of the disturbance wheel
		Random rand = new Random();
		double currentSpeed = 0;
		int itr = 0;		//display which iteration of the PID loop is currently executing
		whTimer.reset();	//start timing when connection is made

		try{

			while (true) 
			{
				int b;
				try
				{
					b = dIn.readInt();
				}
				catch (EOFException e) 
				{
					break;
				}         
				//101 from pc signals to change disturbance power
				//power is a random number between 35 and 75, inclusive
				//limited range helps the machine run smoothly
				//change this to match desiredSpeed in Send.java
				//Note also speed depends on multipliers in getSpeed()
				if(b==101){
					++itr;
					disturbPower = 	rand.nextInt(41) + 35;
					motB.setPower(-disturbPower);	//negate this since wheels face opposite directions
					dOut.writeInt(disturbPower);
					dOut.flush();
				}else{
					//a value other than 101 represents the amount
					//to change the power to the measured wheel
					if(motA.getPower()+b < 100 && motA.getPower()+b > -100)	//account for limitations of motor
						motA.setPower(motA.getPower() + b);
					
					
					//measure new speed and report values back to pc
					currentSpeed = getSpeed(motA);

					dOut.writeInt(whTimer.elapsed());
					dOut.writeDouble(currentSpeed);
					dOut.writeInt(motA.getPower());
					dOut.writeInt(motC.getTachoCount());
					dOut.flush();
					
					//print readings to screen
					LCD.clear();
					LCD.drawString("itr " + itr, 0, 2);
					LCD.drawString("pow " + motA.getPower(), 0, 3);
					LCD.drawString("spd " + currentSpeed, 0, 4);
					LCD.drawString("dst " + disturbPower, 0, 5);
					LCD.refresh();
					
					
				}

			}
			dOut.close();
			dIn.close();
			conn.close();
		}catch(Exception E){
		}
	}

	//returns speed of given motor as a double
	//this formula gives a value that is slightly lower than the
	//motor power if undamped (thus upper bounded by 100)
	//this also depends on the return value multiplier
	double getSpeed(NXTMotor motX){
		motX.resetTachoCount();
		spTimer.reset();
		Delay.msDelay(160);
		//I've played around with the delay to try to find an optimal value
		//the speed is inaccurate at small values due to small tacho count
		//but we don't want it to get too big or cycle time is long. Also note 
		//that certain speeds/differences between wheels can cause the machine to 
		//"jump" around and grind the wheels, which causes even more innacuracies.
		
		
		
		return ((double)motX.getTachoCount() * 120.0 /spTimer.elapsed());
		//the multiplier here is just to get a comfortable range of speed values
	}
}