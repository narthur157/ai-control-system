package framework;

/**
 * Class for logging data output from brick.
 * 
 * @see FileLogger
 * @author Nicholas Arthur
 *
 */
public interface Logger {
	public void logln(String s);
	public void close();
}
