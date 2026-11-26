package com.hoodox.commons.exceptions;

/**
 * What {@link BaseSystemException} and {@link BaseAppException} have in common.
 */
public interface IBaseException {

	/**
	 * @return the error code, or null if the exception was created without one
	 */
	String getErrorCode();

	String getMessage();

	/**
	 * @return true once {@link #setLogged()} has been called
	 */
	boolean isLogged();

	/**
	 * Marks the exception as already written to the log.
	 */
	void setLogged();
}
