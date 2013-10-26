/**
 * 
 */
package com.hoodox.operon.exceptions;

import com.hoodox.commons.exceptions.BaseAppException;
import com.hoodox.commons.exceptions.ErrorCode;

/**
 * <p>This is used by the action classes if an Application Exception occured.</p>
 * 
 * 
 * @author huac
 *
 */
public class ActionExecutionException extends BaseAppException {

	private static final long serialVersionUID = 3218302690763543025L;

	/**
	 * @param arg0
	 */
	public ActionExecutionException(String arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public ActionExecutionException(ErrorCode arg0, String arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 */
	public ActionExecutionException(ErrorCode arg0, String arg1, Throwable arg2) {
		super(arg0, arg1, arg2);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 */
	public ActionExecutionException(Throwable arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

}
