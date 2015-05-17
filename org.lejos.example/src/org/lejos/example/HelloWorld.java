package org.lejos.example;

import lejos.nxt.Button;
import lejos.nxt.Motor;

/**
 * Example leJOS Project with an ant build file
 *
 */
public class HelloWorld {

	public static void main(String[] args) {
		System.out.println("Hello World");
		Motor.A.forward();
		Button.waitForAnyPress();
	}
}
