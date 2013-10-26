package com.hoodox.operon.exceptions;

import com.hoodox.commons.exceptions.BaseAppException;
import com.hoodox.commons.exceptions.ErrorCode;

public class CaseAlreadyExistException extends BaseAppException {

	private static final long serialVersionUID = -7256581014924471960L;

	public CaseAlreadyExistException(String arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public CaseAlreadyExistException(ErrorCode arg0, String arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}

	public CaseAlreadyExistException(ErrorCode arg0, String arg1, Throwable arg2) {
		super(arg0, arg1, arg2);
		// TODO Auto-generated constructor stub
	}

	public CaseAlreadyExistException(Throwable arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

}
