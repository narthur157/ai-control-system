package org.lejos.example;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;

import lejos.nxt.LCD;
import lejos.nxt.MotorPort;
import lejos.nxt.NXTMotor;
import lejos.nxt.comm.USB;
import lejos.nxt.comm.USBConnection;

/**
 * Test of Java streams over USB.
 * Run the PC example, USBSend, to send data.
 * 
 * @author Lawrie Griffiths
 *
 */
public class ShuffleRec {

	public static void main(String [] args) throws Exception 
	{
		LCD.drawString("waiting", 0, 0);
		USBConnection conn = USB.waitForConnection();
		DataOutputStream dOut = conn.openDataOutputStream();
		DataInputStream dIn = conn.openDataInputStream();
		
		
		NXTMotor motA = new NXTMotor(MotorPort.A);
		NXTMotor motB = new NXTMotor(MotorPort.B);
		NXTMotor motC = new NXTMotor(MotorPort.C);
		
		
		while (true) 
		{
            int b;
            int c;
            int d;
            try
            {
                b = dIn.readInt();
                c = -b/11*10;
                d = b%11*10;
            }
            catch (EOFException e) 
            {
                break;
            }         
			//dOut.writeInt(-b);
			//dOut.flush();
	        //LCD.drawInt(b,8,0,1);
            //LCD.clear();
            //LCD.drawInt(c, 0, 2);
            //LCD.drawInt(d, 0, 3);
            
            if(c+d < 50 && c+d > -50){
            	motA.setPower(c);
            	motB.setPower(d);
                LCD.clear();
                LCD.drawInt(c, 0, 2);
                LCD.drawInt(d, 0, 3);
            }
            else{
            	LCD.drawString("no change", 0, 4);
                LCD.drawInt(c, 0, 5);
                LCD.drawInt(d, 0, 6);
            }
            
            dOut.writeInt(d-c);
            dOut.flush();
            
            
		}
        dOut.close();
        dIn.close();
        conn.close();
	}
}

