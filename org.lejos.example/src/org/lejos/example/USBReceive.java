package org.lejos.example;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;

import lejos.nxt.LCD;
import lejos.nxt.comm.USB;
import lejos.nxt.comm.USBConnection;

/**
 * Test of Java streams over USB.
 * Run the PC example, USBSend, to send data.
 * 
 * @author Lawrie Griffiths
 *
 */
public class USBReceive {

	public static void main(String [] args) throws Exception 
	{
		LCD.drawString("waiting", 0, 0);
		USBConnection conn = USB.waitForConnection();
		DataOutputStream dOut = conn.openDataOutputStream();
		DataInputStream dIn = conn.openDataInputStream();
		
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
			dOut.writeInt(-b);
			dOut.flush();
	        LCD.drawInt(b,8,0,1);
		}
        dOut.close();
        dIn.close();
        conn.close();
	}
}

