package com.hoodox.operon.exceptions;

import com.hoodox.commons.exceptions.BaseAppException;
import com.hoodox.commons.exceptions.ErrorCode;

public class CaseNotExistException extends BaseAppException {

	private static final long serialVersionUID = -1698891385531344296L;

	public CaseNotExistException(String arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public CaseNotExistException(ErrorCode arg0, String arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}

	public CaseNotExistException(ErrorCode arg0, String arg1, Throwable arg2) {
		super(arg0, arg1, arg2);
		// TODO Auto-generated constructor stub
	}

	public CaseNotExistException(Throwable arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

}
