package com.hoodox.operon.exceptions;

import com.hoodox.commons.exceptions.BaseAppException;
import com.hoodox.commons.exceptions.ErrorCode;

public class NotRootCaseException extends BaseAppException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -253814162779907895L;

	public NotRootCaseException(String arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public NotRootCaseException(ErrorCode arg0, String arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}

	public NotRootCaseException(ErrorCode arg0, String arg1, Throwable arg2) {
		super(arg0, arg1, arg2);
		// TODO Auto-generated constructor stub
	}

	public NotRootCaseException(Throwable arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

}
