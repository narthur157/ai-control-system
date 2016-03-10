/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package annclient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.Socket;

/**
 *
 * @author millerti
 */
public class AnnClient {
    Socket sock;
    PrintWriter writer;
    BufferedReader reader;
    
    void init(String addr, int port) throws IOException {
        sock = new Socket(addr, port);
        writer = new PrintWriter(new OutputStreamWriter(sock.getOutputStream(), "UTF8"));
        reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
    }
    
    void sendLine(String s) {
        writer.println(s);
        writer.flush();
        System.out.println("Sending: " + s);
    }
    
    void sendNumbers(double[] inputs) {
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<inputs.length; i++) {
            sb.append(String.format("%.40f", inputs[i]));
            if (i != inputs.length-1) sb.append(' ');
        }
        sendLine(sb.toString());
    }
    
    String getLine() throws IOException {
        String line = null;
        line = reader.readLine();
        return line;
    }
    
    double[] getNumbers() throws IOException {
        String line = getLine();
        String[] line_split = line.split(" ");
        double[] numbers = new double[line_split.length];
        for (int i=0; i<line_split.length; i++) {
            numbers[i] = Double.parseDouble(line_split[i]);
        }
        return numbers;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        AnnClient ann = new AnnClient();
        ann.init("127.0.0.1", 8888);
        double[] d = {1, 2, 3};
        ann.sendNumbers(d);
        double[] e;
        e = ann.getNumbers();
        for (int i=0; i<e.length; i++) {
            System.out.println(e[i]);            
        }
    }
    
}
