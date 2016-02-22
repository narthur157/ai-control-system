package client;

import java.io.EOFException;
import java.util.Random;

import lejos.nxt.LCD;
import lejos.nxt.MotorPort;
import lejos.nxt.NXTMotor;
import lejos.util.Stopwatch;

public class BrickController {
	private PCComm comm;
	
	private double currentSpeed = 0;
	
	Random rand = new Random();
	

	NXTMotor controlWheel = new NXTMotor(MotorPort.A),	//use port A for the wheel we want to control
			 disturbWheel = new NXTMotor(MotorPort.B),	//use port B for the disturbance wheel
			 torqueArm 	  =	new NXTMotor(MotorPort.C);	//use port C for the arm angle

	WheelTimer disturbTimer = new WheelTimer(disturbWheel);
	Stopwatch procTimer = new Stopwatch();		//times the WHole process


	public BrickController(PCComm commInit) {
		comm = commInit;
	}
	
	public void start() throws Exception {
		disturbTimer.start();
		procTimer.reset();	//start timing when connection is made
		while (true) {
			try {
				Command c = comm.receiveCommand();
				NXTMotor motor;
				
				if (c.motor == Command.CONTROL_WHEEL) {
					motor = controlWheel;
				}
				else if (c.motor == Command.DISTURB_WHEEL) {
					motor = disturbWheel;
				}
				else if (c.motor == Command.TORQUE_ARM) {
					motor = torqueArm;
				}
				else {
					LCD.clear();
					LCD.drawString("Invalid command, bad motor", 1, 1);
					break;
				}
				
				motor.setPower(c.power);
			}
			catch (EOFException e) {
				LCD.clear();
				LCD.drawString("EOFE Exception", 1, 1);
				//Delay.msDelay(5000);
				break;
			}  
			
			printState();
		}
	}
	
	private void printState() {
		//print readings to screen
		LCD.clear();
		LCD.drawString("ctrlPow " + controlWheel.getPower(), 0, 3);
		LCD.drawString("curSpd " + currentSpeed, 0, 4);
		LCD.drawString("distrbPow " + disturbWheel.getPower(), 0, 5);
		LCD.refresh();
	}
	
	public BrickState getState() {
		return new BrickState(procTimer.elapsed(), disturbTimer.getSpeed(), 
							  disturbWheel.getPower(), controlWheel.getPower(), 
							  torqueArm.getTachoCount());
	}
}
