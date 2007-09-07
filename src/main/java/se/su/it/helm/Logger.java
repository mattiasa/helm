package se.su.it.helm;

import java.io.PrintWriter;
import java.io.StringWriter;

public class Logger {

	boolean debug = true;
	
	public void debug(String msg) {
		if(debug) {
			System.err.println(msg);
		}
	}
	
	public void log(String msg) {
		System.err.println(msg);
	}

	public void error(String msg) {
		System.err.println(msg);
		
	}
}
