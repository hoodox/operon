package com.hoodox.operon.exceptions;

import com.hoodox.commons.exceptions.BaseAppException;
import com.hoodox.commons.exceptions.ErrorCode;

public class TokenNotExistException extends BaseAppException {

	private static final long serialVersionUID = -8723367897274538673L;

	public TokenNotExistException(String arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public TokenNotExistException(ErrorCode arg0, String arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}

	public TokenNotExistException(ErrorCode arg0, String arg1, Throwable arg2) {
		super(arg0, arg1, arg2);
		// TODO Auto-generated constructor stub
	}

	public TokenNotExistException(Throwable arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

}
