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
    
    // Static block called as soon as this class is referenced
    static {
    	try {
			init("127.0.0.1", 8888);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    private static void init(String addr, int port) throws IOException {
        sock = new Socket(addr, port);
        writer = new PrintWriter(new OutputStreamWriter(sock.getOutputStream(), "UTF8"));
        reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
    }
    
    private static void sendLine(int targetSpeed, BrickState bs) {
    	sendNumbers(new double[]{ bs.disturbSpeed, bs.angle, bs.controlPower, bs.controlPower, targetSpeed });
    }
    
    private static void sendLine(String s) {
        writer.println(s);
        writer.flush();
    }
    
    /**
     * 
     * @param inputs - must be length 4 in order LdSpd, Angle, CtrlPwr, TargetSpeed
     */
    private static void sendNumbers(double[] inputs) {
    	if (inputs.length != 5) {
    		System.err.println("Invalid input size to neural net");
    	}
    	
        StringBuilder sb = new StringBuilder();
        
        for (int i=0; i<inputs.length; i++) {
            sb.append(String.format("%.40f", inputs[i]));
            if (i != inputs.length-1) sb.append(' ');
        }

        sendLine(sb.toString());
    }
    
    private static String getLine() throws IOException {
        String line = null;
        line = reader.readLine();
        
        return line;
    }
    
    /**
     * Replaced old getNumbers method, neural net search
     * only ever outputs a single int. For old code parsing
     * multiple doubles, check commits on this file prior to May 10th
     * 
     * @return Power suggestion from neural net
     * @throws IOException
     */
    private static int getResponse() throws IOException {
    	return Integer.parseInt(getLine().trim());
    }
    
    /**
     * Send a call to the ann server to process inputs and return suggested power
     * @param targetSpeed Speed trying to be set 
     * @param bs BrickState holding inputs to the ann
     * @return The suggested speed
     * @throws IOException
     */
	public static int searchSpeed(int targetSpeed, BrickState bs) throws IOException {
    	sendLine(targetSpeed, bs);
    	return getResponse();
	}
}
