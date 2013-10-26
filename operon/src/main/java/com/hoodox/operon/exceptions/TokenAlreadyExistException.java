package com.hoodox.operon.exceptions;

import com.hoodox.commons.exceptions.BaseAppException;
import com.hoodox.commons.exceptions.ErrorCode;

public class TokenAlreadyExistException extends BaseAppException {

	private static final long serialVersionUID = -4676449062642391496L;

	public TokenAlreadyExistException(String arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public TokenAlreadyExistException(ErrorCode arg0, String arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}

	public TokenAlreadyExistException(ErrorCode arg0, String arg1,
			Throwable arg2) {
		super(arg0, arg1, arg2);
		// TODO Auto-generated constructor stub
	}

	public TokenAlreadyExistException(Throwable arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

}
