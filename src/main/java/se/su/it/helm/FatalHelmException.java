/**
 * 
 */
package se.su.it.helm;

/**
 * @author mattiasa
 *
 */
public class FatalHelmException extends HelmException {
	public FatalHelmException(String msg, Exception e) {
		super(msg, e);
	}
}
