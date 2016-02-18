package pid;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import server.Logger;

public class PIDLogger implements Logger {
	private PrintWriter out;
	
	public PIDLogger() throws IOException {
		out = new PrintWriter(new FileWriter("/home/bu/PIDdata.txt"));
	}
	
	public void logln(String s) {
		out.println(s);
	}
}
