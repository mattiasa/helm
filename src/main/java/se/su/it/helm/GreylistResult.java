package se.su.it.helm;

public class GreylistResult {
	private boolean pass = false;
	String message = null;
	public GreylistResult(boolean pass) {
		this.pass = pass;
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
