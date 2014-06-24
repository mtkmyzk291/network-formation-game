package basic.preparation;

/**
 * ÉmÅ[ÉhÇ™Ç»Ç¢Ç±Ç∆Çé¶Ç∑Exception
 * @author tori
 *
 */
public class NoNodeException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7565766054386352789L;
	/**
	 * @param arg0
	 */
	public NoNodeException(String arg0) {
		super(arg0);
	}
	/**
	 * @param arg0
	 * @param arg1
	 */
	public NoNodeException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}
	/**
	 * @param arg0
	 */
	public NoNodeException(Throwable arg0) {
		super(arg0);
	}
	/**
	 * 
	 */
	public NoNodeException() {
		super();
	}
}
