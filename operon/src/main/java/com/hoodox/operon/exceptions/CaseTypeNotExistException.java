/**
 * 
 */
package com.hoodox.operon.exceptions;

import com.hoodox.commons.exceptions.BaseAppException;
import com.hoodox.commons.exceptions.ErrorCode;

/**
 * @author huac
 *
 */
public class CaseTypeNotExistException extends BaseAppException {

	private static final long serialVersionUID = -4962054427790566894L;

	/**
	 * @param arg0
	 */
	public CaseTypeNotExistException(String arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public CaseTypeNotExistException(ErrorCode arg0, String arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 */
	public CaseTypeNotExistException(ErrorCode arg0, String arg1, Throwable arg2) {
		super(arg0, arg1, arg2);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 */
	public CaseTypeNotExistException(Throwable arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

}
