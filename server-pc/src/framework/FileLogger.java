package framework;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Creates file with name as date string, logs data from brick
 * @author Nicholas Arthur
 *
 */
public class FileLogger implements Logger {
	private PrintWriter out;
	
	/**
	 * Column headers, separated by a tab
	 */
	protected String columns = "";
	
	/**
	 * Directory without a slash at the end of where to put output
	 */
	protected String directory = "";
	
	// creates a string for a most-likely unique file
	// easily sortable in filesystem by alphanumeric ordering
	// this way you can do many test runs without deleting old tests
	// and easily know which tests are which
	private static String timestampFile() {
		// credit: http://stackoverflow.com/questions/5683728/convert-java-util-date-to-string
		// Create an instance of SimpleDateFormat used for formatting 
		// the string representation of date (month/day/year)
		DateFormat df = new SimpleDateFormat("MM-dd-HH-mm");

		// Get the date today using Calendar object.
		Date today = Calendar.getInstance().getTime();        
		// Using DateFormat format method we can create a string 
		// representation of a date with the defined format.
		return df.format(today) + "-data.csv";
	}
	
	public FileLogger() throws IOException {
		this("testRuns", "Time\tLdSpd\tLdPwr\tCtrlPwr\tTrqPwr\tAngle\tStablePwr\tFlag", timestampFile());
	}
	
	public FileLogger(String directory, String columns, String fileName) throws IOException {
		this.directory = directory;
		this.columns = columns;
		
		File f = new File(directory + "/" + fileName);
		
		if (f.exists()) {
			// May want to check with the user here
			f.delete();
		}
		
		out = new PrintWriter(new FileWriter(f));
		logln(columns);
	}
	
	/**
	 * Writes string to file exactly as given
	 */
	public void logln(String s) {
		out.println(s);
	}
	
	// final methods cannot be overriden
	final public void close() {
		out.close();
	}
}
