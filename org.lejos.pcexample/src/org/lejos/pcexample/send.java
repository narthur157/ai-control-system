package org.lejos.pcexample;

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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import lejos.pc.comm.NXTConnector;//imports

public class send {

	JButton shutdown;// a lot of objects are created
	JButton options;
	JButton disconnect;
	JButton reconnect;
	JFrame frame;
	JTextArea console;
	JTextArea keyArea;
	JPanel buttonPanel;
	JPanel textPanel;
	JScrollPane pScroll;
	DataOutputStream dos;
	static DataInputStream dis;
	static boolean disconnected = true;

	public static void main(String[] args) {

		send send = new send();
		send.buildGUI();// GUI Building
		send.connect();// Start connection
		//Thread.
		 

		
//		for (int i = 0; i < 10; i++) {
//			Thread thread = new Thread(){
//			    public void run(){
//			      System.out.println("Thread Running");
//					while (!disconnected) {
//						try {
////							try {
////								Thread.sleep(1000);
////							} catch (InterruptedException e) {
////								// TODO Auto-generated catch block
////								//e.printStackTrace();
////							}
//							Thread.yield();
//							System.out.println(""+dis.readInt());
//						} catch (IOException e) {
//							// TODO Auto-generated catch block
//							//e.printStackTrace();
//						}
//					}
//			    }
//			  };
//			thread.start();	
//			System.out.println("???");
//			
//			thread.interrupt();
//			try {
//				Thread.sleep(1000);
//			} catch (InterruptedException e) {
//				//e.printStackTrace();
//				System.out.println("meh");
//			}
//			
//		}

//		while (!disconnected) {
//			try {
//				Thread.sleep(1);
//			} catch (InterruptedException e1) {
//				e1.printStackTrace();
//			}
//			try {
//				if (dis.available() > 0) {
//					System.out.println("trying to read");
//					int val = dis.readInt();
//					int command = val >> 8;
//					if (command == 0) {
//						System.out.println("Motor A tacho count: " + val);
//					} else if (command == 1) {
//						System.out.println("Motor B tacho count: " + val);
//					} else if (command == 2) {
//						System.out.println("Motor C tacho count: " + val);
//					} else {
//						System.out.println("com: " + command + " val: " + val);
//					}
//				}
//			} catch (IOException e) {
//				System.out.println("?!?!?!?!?!");
//				e.printStackTrace();
//			}
//		}
	}

	public void buildGUI() // Simple GUI building
	{

		frame = new JFrame("Controller: By Aravind"); // new JFrame
		frame.setSize(624, 350);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);

		buttonPanel = new JPanel(new GridLayout(2, 2)); // Some JPanels
		textPanel = new JPanel(new GridLayout(1, 2));

		shutdown = new JButton("Shutdown"); // Four new buttons
		shutdown.addActionListener(new shutdownListener());
		options = new JButton("Options");
		disconnect = new JButton("Disconnect");
		disconnect.addActionListener(new disconnectListener());
		reconnect = new JButton("Reconnect");
		reconnect.addActionListener(new reconnectListener());

		buttonPanel.add(reconnect);
		buttonPanel.add(disconnect);
		buttonPanel.add(shutdown);
		buttonPanel.add(options);

		console = new JTextArea();
		keyArea = new JTextArea();
		keyArea.setPreferredSize(new Dimension(100, 100));
		keyArea.addKeyListener(new keyListener());// add key listener to listen
													// to key events such as
													// press, etc

		pScroll = new JScrollPane(console,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		pScroll.setPreferredSize(new Dimension(500, 250));

		textPanel.add(pScroll);
		textPanel.add(keyArea);

		frame.getContentPane().add(textPanel, BorderLayout.NORTH);
		frame.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		frame.repaint();
	}

	public void connect() {

		NXTConnector conn = new NXTConnector();// create a new NXT connector
		boolean connected = conn.connectTo("usb://"); // try to connect to any
														// NXT over usb

		if (!connected) {// failure
			System.out.println("Failed to connect to any NXT\n");
			System.out.println("Press Reconect to retry.\n");
		}

		else {
			disconnected=false;
			System.out.println("Connected to " + conn.getNXTInfo() + "\n");
			dos = new DataOutputStream(conn.getOutputStream());
			dis = new DataInputStream(conn.getInputStream());

		}

	}

	public void shutdownAction() // shutdown method of shutdown button
	{
		try {
			dos.writeInt(-1);
			dos.flush();
			console.append("NXT shut down. Connection lost.\n");
		}// NXT recognizes input of -1 as signal to shutdown
		catch (Exception e) {
			console.append("Could not send command\n");
		}// catch exception, try to save face:)
	}

	public void disconnectAction() {
		try {
			dos.writeInt(0);
			dos.flush();
			console.append("NXT disconnected. Press Reconnect to reconnect.\n");
			disconnected=true;
		}// NXT recognizes 0 as command to terminate bluetooth connection and
			// seek a new one.
		catch (Exception e) {
			console.append("Could not send command\n");
		}
	}

	class shutdownListener implements ActionListener // listeners for three
														// buttons, options
														// button does not do
														// anything yet
	{
		public void actionPerformed(ActionEvent evt) {
			shutdownAction();
		}
	}

	class disconnectListener implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			disconnectAction();
		}
	}

	class reconnectListener implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			connect();// call main connect method to try to connect again
		}
	}

	class keyListener implements KeyListener// three of the methods that have to
											// be implemented in keylistener, we
											// only need keyPressed and
											// keyReleased
	{

		public void keyTyped(KeyEvent e) {

		}

		public void keyPressed(KeyEvent e) {
			final int RIGHT=39, LEFT=37, UP=38, DOWN=40;
			
			try {
				if (e.getKeyCode() == RIGHT)// right arrow keycode, tell NXT to
				{
					console.append("Right command sent.\n");
					dos.writeInt(e.getKeyCode());
					dos.flush();
				} else if (e.getKeyCode() == UP)// up arrow keycode, tell NXT to
				{
					console.append("Forward command sent.\n");
					dos.writeInt(e.getKeyCode());
					dos.flush();
				} else if (e.getKeyCode() == LEFT)// left arrow keycode, tell NXT
				{
					console.append("Left command sent.\n");
					dos.writeInt(e.getKeyCode());
					dos.flush();
				} else if (e.getKeyCode() == DOWN)// down arrow keycode, tell NXT
				{
					console.append("Backwards command sent.\n");
					dos.writeInt(e.getKeyCode());
					dos.flush();
				} else {
					console.append("Unrecognized command.\n");
				}// some other key pressed, not any of the four arrow keys, warn
					// that the command is unrecognized
			}

			catch (Exception E) {
				console.append("Could not send command\n");
			}// catch exception, save face
		}

		public void keyReleased(KeyEvent e) {
			try {
				dos.writeInt(-2);// NXT recognises -2 as command to stop
									// whatever it is doing whether it is
									// turning left, right or going forwards or
									// backwards.
				dos.flush();
			} catch (Exception E) {
			}
		}
	}

}
