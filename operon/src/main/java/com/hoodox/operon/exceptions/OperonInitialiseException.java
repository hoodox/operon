package com.hoodox.operon.exceptions;

import com.hoodox.commons.exceptions.BaseSystemException;
import com.hoodox.commons.exceptions.ErrorCode;

/**
 * This is thrown when the Operon framework is not properly initialised
 * @author Chung
 *
 */
public class OperonInitialiseException extends BaseSystemException {

	private static final long serialVersionUID = 2056955320767990337L;

	public OperonInitialiseException(String arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public OperonInitialiseException(ErrorCode arg0, String arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}

	public OperonInitialiseException(ErrorCode arg0, String arg1, Throwable arg2) {
		super(arg0, arg1, arg2);
		// TODO Auto-generated constructor stub
	}

	public OperonInitialiseException(Throwable arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

}
