package testing;

import java.io.IOException;

import framework.FileLogger;

/**
 * Compares the output of 2 motor controllers
 * @author Nicholas Arthur
 *
 */
public class ComparisonLogger extends FileLogger {
	public ComparisonLogger(String name) throws IOException {
		super("benchmarks", "#Time\tSpeed\tDisturbance", name);
	}
}
