package com.hoodox.operon.exceptions;

import com.hoodox.commons.exceptions.BaseAppException;
import com.hoodox.commons.exceptions.ErrorCode;

public class InvalidManualOperationException extends BaseAppException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5988504820567730545L;

	public InvalidManualOperationException(String arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public InvalidManualOperationException(ErrorCode arg0, String arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}

	public InvalidManualOperationException(ErrorCode arg0, String arg1, Throwable arg2) {
		super(arg0, arg1, arg2);
		// TODO Auto-generated constructor stub
	}

	public InvalidManualOperationException(Throwable arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

}
