package org.lejos.pcexample;

import java.util.ArrayList;
import java.util.Collections;

public class shuffle {
	public static void main(String[] args){
		//for each int x in powers, load motor power is x/11*10
		//and drive motor power is x%11*10
		ArrayList<Integer> powers = new ArrayList<Integer>();
		for(int i = 0; i < 121; i++){
			powers.add(new Integer(i));
		}
		System.out.println(powers);
		System.out.println(powers.get(0)/11*10 + " " +powers.get(0)%11*10);
		Collections.shuffle(powers);
		System.out.println(powers);
		System.out.println(powers.get(0)/11*10 + " " +powers.get(0)%11*10);
	}
}
