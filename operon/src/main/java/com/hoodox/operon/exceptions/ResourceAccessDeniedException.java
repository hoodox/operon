/**
 * 
 */
package com.hoodox.operon.exceptions;

import com.hoodox.commons.exceptions.BaseAppException;
import com.hoodox.commons.exceptions.ErrorCode;

/**
 * This is thrown if the TriggerResource is not allowed to open/execute
 * a Case or WorkItem/Activity
 * @author huac
 *
 */
public class ResourceAccessDeniedException extends BaseAppException {

	private static final long serialVersionUID = 6660575554160701580L;

	/**
	 * @param arg0
	 */
	public ResourceAccessDeniedException(String arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public ResourceAccessDeniedException(ErrorCode arg0, String arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 */
	public ResourceAccessDeniedException(ErrorCode arg0, String arg1,
			Throwable arg2) {
		super(arg0, arg1, arg2);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 */
	public ResourceAccessDeniedException(Throwable arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

}
