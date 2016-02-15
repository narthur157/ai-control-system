package org.lejos.pcexample;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

import javax.swing.Timer;

public class FileTest{
	public static void main(String[] args) throws IOException, InterruptedException{
		final PrintWriter out = new PrintWriter(new FileWriter("/home/bu/outtest2.txt")); 
		//out.print("Hello "); 
		//out.println("world"); 
		//out.print("\n-----------------\n");
		//out.println(System.currentTimeMillis());
		//double number = 1.0/3.0;
		//out.println(number);
		
		/*int delay = 10;
		ActionListener taskPerformer = new ActionListener(){
			//PrintWriter out = new PrintWriter(new FileWriter("/home/bu/outtest2.txt"));
			int i = 0;
			public void actionPerformed(ActionEvent e) {
				out.print(i);
				i++;
				if(i>=10){
					out.print('\n');
					i  = 0;
				}
				
			}
		};
		new Timer(delay,taskPerformer).start();
		TimeUnit.SECONDS.sleep(10);*/
		
		out.println(2);
		out.close();
	}

}
