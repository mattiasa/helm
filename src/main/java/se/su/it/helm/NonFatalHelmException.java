package se.su.it.helm;

public class NonFatalHelmException extends HelmException {

	public NonFatalHelmException(String msg, Exception e) {
		super(msg, e);
		
	}
}
