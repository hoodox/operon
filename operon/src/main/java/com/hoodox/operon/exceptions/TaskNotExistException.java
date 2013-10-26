package com.hoodox.operon.exceptions;

import com.hoodox.commons.exceptions.BaseAppException;
import com.hoodox.commons.exceptions.ErrorCode;

public class TaskNotExistException extends BaseAppException {

	private static final long serialVersionUID = 8165393883058762869L;

	public TaskNotExistException(String arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public TaskNotExistException(ErrorCode arg0, String arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}

	public TaskNotExistException(ErrorCode arg0, String arg1,
			Throwable arg2) {
		super(arg0, arg1, arg2);
		// TODO Auto-generated constructor stub
	}

	public TaskNotExistException(Throwable arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

}
