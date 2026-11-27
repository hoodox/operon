package com.hoodox.commons.exceptions;

/**
 * An XML document could not be read or parsed.
 */
public class XMLAccessException extends BaseAppException {

	private static final long serialVersionUID = 1L;

	public XMLAccessException(String message) {
		super(message);
	}

	public XMLAccessException(ErrorCode errorCode, String message) {
		super(errorCode, message);
	}

	public XMLAccessException(ErrorCode errorCode, String message, Throwable cause) {
		super(errorCode, message, cause);
	}

	public XMLAccessException(Throwable cause) {
		super(cause);
	}
}
