package com.hoodox.operon.exceptions;

import com.hoodox.commons.exceptions.BaseSystemException;
import com.hoodox.commons.exceptions.ErrorCode;

/**
 * This exception is throuwn when a configuration exception occurs.
 * 
 * @author Huac
 * 
 */
public class OperonConfigException extends BaseSystemException {

	private static final long serialVersionUID = -1924526139122708337L;


	/**
	 * @param errorCode -
	 *            the error string
	 * 
	 * @param errString -
	 *            the error code
	 * 
	 */
	public OperonConfigException(ErrorCode errorCode, String errString) {
		super(errorCode, errString);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param errorCode
	 * @param errString
	 * @param ex
	 */
	public OperonConfigException(ErrorCode errorCode, String errString, Throwable ex) {
		super(errorCode, errString, ex);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param ex
	 */
	public OperonConfigException(Throwable ex) {
		super(ex);
		// TODO Auto-generated constructor stub
	}

}
