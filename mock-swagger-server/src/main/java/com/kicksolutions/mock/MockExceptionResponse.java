package com.kicksolutions.mock;

/**
 * 
 * @author MSANTOSH
 *
 */
public class MockExceptionResponse {
	private String code;
	private String link;
	private String message;
	private String rel;
	private String traceid;
	/**
	 * @return the code
	 */
	public String getCode() {
		return code;
	}
	/**
	 * @param code the code to set
	 */
	public void setCode(String code) {
		this.code = code;
	}
	/**
	 * @return the link
	 */
	public String getLink() {
		return link;
	}
	/**
	 * @param link the link to set
	 */
	public void setLink(String link) {
		this.link = link;
	}
	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}
	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}
	/**
	 * @return the rel
	 */
	public String getRel() {
		return rel;
	}
	/**
	 * @param rel the rel to set
	 */
	public void setRel(String rel) {
		this.rel = rel;
	}
	/**
	 * @return the traceid
	 */
	public String getTraceid() {
		return traceid;
	}
	/**
	 * @param traceid the traceid to set
	 */
	public void setTraceid(String traceid) {
		this.traceid = traceid;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "MockException [code=" + code + ", link=" + link + ", message=" + message + ", rel=" + rel + ", traceid="
				+ traceid + "]";
	}
}
