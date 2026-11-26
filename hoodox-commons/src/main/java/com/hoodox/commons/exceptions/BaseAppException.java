package com.hoodox.commons.exceptions;

/**
 * Base class of checked exceptions.
 */
public class BaseAppException extends Exception implements IBaseException {

	private static final long serialVersionUID = 1L;

	private final ErrorCode errorCode;
	private boolean logged;

	public BaseAppException(String message) {
		super(message);
		this.errorCode = null;
	}

	public BaseAppException(ErrorCode errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}

	public BaseAppException(ErrorCode errorCode, String message, Throwable cause) {
		super(message, cause);
		this.errorCode = errorCode;
	}

	public BaseAppException(Throwable cause) {
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
