package se.su.it.helm;

import java.io.PrintWriter;
import java.io.StringWriter;

public class HelmException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4744769495510903736L;
	Exception cause;
	String msg;
	
	public HelmException(String msg, Exception cause) {
		super();
		this.cause = cause;
		this.msg = msg;
	}

	private String stackTracePrinter(Exception e) {
		StringWriter sWriter = new StringWriter();
		PrintWriter pWriter = new PrintWriter(sWriter);
		e.printStackTrace(pWriter);
		pWriter.close();
		
		return sWriter.toString();
	}
	
	public String getString() {
		String ret = "";
		ret += "Exception " + this.getClass().getName() + "\n";
		ret += "Message: " + this.msg + "\n";
		ret += "Stack trace: \n";
		ret += stackTracePrinter(this);
		if(cause != null) {
			ret += "caused by \n";
			ret += stackTracePrinter(cause);
		}
		return ret;
	}
}
