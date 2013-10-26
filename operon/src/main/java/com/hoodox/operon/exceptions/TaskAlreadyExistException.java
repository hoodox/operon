/**
 * 
 */
package com.hoodox.operon.exceptions;

import com.hoodox.commons.exceptions.BaseAppException;
import com.hoodox.commons.exceptions.ErrorCode;

/**
 * @author HUAC
 *
 */
public class TaskAlreadyExistException extends BaseAppException {

	private static final long serialVersionUID = 5507098511564442209L;

	/**
	 * @param arg0
	 */
	public TaskAlreadyExistException(String arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public TaskAlreadyExistException(ErrorCode arg0, String arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 */
	public TaskAlreadyExistException(ErrorCode arg0, String arg1,
			Throwable arg2) {
		super(arg0, arg1, arg2);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 */
	public TaskAlreadyExistException(Throwable arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

}
