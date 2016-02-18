package server;

// might not be very useful as an interface, all implementations similar thus far
public interface Logger {
	public void logln(String s);
	public void close();
}
