package se.su.it.helm;

public class GreylistResult {
	private boolean pass = false;
	String message = null;
	
	public GreylistResult(boolean pass, String message) {
		this.message = message;
		this.pass = pass;
	}
	
	public GreylistResult(boolean pass) {
		this(pass, null);
	}
	
	boolean passmail() {
		return pass;
	}
	void setMessage(String message) {
		this.message = message;
	}
	String getMessage() {
		return message;
	}
	
	
}
