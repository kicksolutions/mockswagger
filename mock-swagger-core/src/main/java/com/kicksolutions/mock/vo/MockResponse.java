/**
 * 
 */
package com.kicksolutions.mock.vo;

/**
 * @author MSANTOSH
 *
 */
public class MockResponse {
	private String responseCode;
	private Object example;
	private String message;
	
	/**
	 * @param responseCode
	 * @param example
	 * @param message
	 */
	public MockResponse(String responseCode, Object example, String message) {
		super();
		this.responseCode = responseCode;
		this.example = example;
		this.message = message;
	}

	/**
	 * @return the responseCode
	 */
	public String getResponseCode() {
		return responseCode;
	}

	/**
	 * @return the example
	 */
	public Object getExample() {
		return example;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "MockResponse [responseCode=" + responseCode + ", example=" + example + ", message=" + message + "]";
	}
}