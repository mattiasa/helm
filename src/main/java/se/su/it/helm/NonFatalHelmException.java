package se.su.it.helm;

public class NonFatalHelmException extends HelmException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3768408426979914694L;

	public NonFatalHelmException(String msg, Exception e) {
		super(msg, e);
		
	}
}
