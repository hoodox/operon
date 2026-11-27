package com.hoodox.commons.exceptions;

/**
 * Base class of unchecked exceptions.
 */
public class BaseSystemException extends RuntimeException implements IBaseException {

	private static final long serialVersionUID = 1L;

	private final ErrorCode errorCode;
	private boolean logged;

	public BaseSystemException(String message) {
		super(message);
		this.errorCode = null;
	}

	public BaseSystemException(ErrorCode errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}

	public BaseSystemException(ErrorCode errorCode, String message, Throwable cause) {
		super(message, cause);
		this.errorCode = errorCode;
	}

	public BaseSystemException(Throwable cause) {
		super(cause);
		this.errorCode = null;
	}

	public String getErrorCode() {
		return errorCode == null ? null : errorCode.getCode();
	}

	public boolean isLogged() {
		return logged;
	}

	public void setLogged() {
		this.logged = true;
	}
}
