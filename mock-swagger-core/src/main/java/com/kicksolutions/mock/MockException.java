/**
 * 
 */
package com.kicksolutions.mock;

/**
 * @author MSANTOSH
 *
 */
public class MockException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8309060152024106599L;
	private String method;
	private String URI;
	
	public MockException() {
	}

	public MockException(String method,String URI) {
		super("METHOD " + method + " Not Supported for URI "+ URI);
		this.method = method;
		this.URI = URI;
	}

	public MockException(String method,String URI, Throwable arg1) {
		super("METHOD " + method + " Not Supported for URI "+ URI, arg1);
		this.method = method;
		this.URI = URI;		
	}

	/**
	 * @return the method
	 */
	public String getMethod() {
		return method;
	}

	/**
	 * @return the uRI
	 */
	public String getURI() {
		return URI;
	}
}