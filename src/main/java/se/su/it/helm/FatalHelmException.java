/**
 * 
 */
package se.su.it.helm;

/**
 * @author mattiasa
 *
 */
public class FatalHelmException extends HelmException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2329471907357340359L;

	public FatalHelmException(String msg, Exception e) {
		super(msg, e);
	}
}
