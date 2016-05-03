/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package neural;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import framework.BrickState;

/**
 *
 * @author millerti
 */
public class AnnClient {
    private static Socket sock;
    private static PrintWriter writer;
    private static BufferedReader reader;
    
    static {
    	try {
			init("127.0.0.1", 8888);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    static void init(String addr, int port) throws IOException {
        sock = new Socket(addr, port);
        writer = new PrintWriter(new OutputStreamWriter(sock.getOutputStream(), "UTF8"));
        reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
    }
    
    public static double[] testInputs(int targetSpeed, BrickState bs) throws IOException {
    	sendLine(targetSpeed, bs);
    	return getNumbers();
    }
    
    static void sendLine(int targetSpeed, BrickState bs) {
    	//System.out.println("targetSpeed: " + targetSpeed + " bs: " + bs.toString());
    	sendNumbers(new double[]{ bs.disturbSpeed, bs.angle, bs.controlPower, targetSpeed });
    }
    
    static void sendLine(String s) {
        writer.println(s);
        writer.flush();
        //System.out.println("Sending (normalized): " + s);
    }
    
    /**
     * 
     * @param inputs - must be length 4 in order LdSpd, Angle, CtrlPwr, TargetSpeed
     */
    static void sendNumbers(double[] inputs) {
    	if (inputs.length != 4) {
    		System.err.println("Invalid input size to neural net");
    	}
    
    	// Normalization now occurs in eval
//    	inputs[0] = Normalization.normalizeLoad(inputs[0]);
//    	inputs[1] = Normalization.normalizeAngle(inputs[1]);
//    	inputs[2] = Normalization.normalizeControl(inputs[2]);
    	
    	
        StringBuilder sb = new StringBuilder();
        
        for (int i=0; i<inputs.length; i++) {
            sb.append(String.format("%.40f", inputs[i]));
            if (i != inputs.length-1) sb.append(' ');
        }

        sendLine(sb.toString());
    }
    
    static String getLine() throws IOException {
        String line = null;
        line = reader.readLine();
        
        return line;
    }
    
    static double[] getNumbers() throws IOException {
        String line = getLine();
        String[] line_split = line.split(" ");
        double[] numbers = new double[line_split.length];
        
        for (int i=0; i<line_split.length; i++) {
            numbers[i] = Double.parseDouble(line_split[i]);
        }
        
        return numbers;
    }

	public static int searchSpeed(int targetSpeed, BrickState bs) throws IOException {
		return (int) testInputs(targetSpeed, bs)[0];
	}
}
