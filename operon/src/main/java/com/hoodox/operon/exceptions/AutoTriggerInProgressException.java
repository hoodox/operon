package com.hoodox.operon.exceptions;

import com.hoodox.commons.exceptions.BaseAppException;
import com.hoodox.commons.exceptions.ErrorCode;

public class AutoTriggerInProgressException extends BaseAppException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5947159280306947121L;

	public AutoTriggerInProgressException(String arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public AutoTriggerInProgressException(ErrorCode arg0, String arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}

	public AutoTriggerInProgressException(ErrorCode arg0, String arg1, Throwable arg2) {
		super(arg0, arg1, arg2);
		// TODO Auto-generated constructor stub
	}

	public AutoTriggerInProgressException(Throwable arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

}
