package se.su.it.helm;

public class TerminatingHelmException extends HelmException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2734649498852185070L;

	public TerminatingHelmException(String msg, Exception e) {
		super(msg, e);
	}
}
