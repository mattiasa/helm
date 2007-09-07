package se.su.it.helm;

public class TerminatingHelmException extends HelmException {

	public TerminatingHelmException(String msg, Exception e) {
		super(msg, e);
	}
}
