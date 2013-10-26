/**
 * 
 */
package com.hoodox.operon.exceptions;

import com.hoodox.commons.exceptions.BaseSystemException;
import com.hoodox.commons.exceptions.ErrorCode;

/**
 * @author huac
 *
 */
public class SetAttributeException extends BaseSystemException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8868124684596666703L;

	/**
	 * @param arg0
	 */
	public SetAttributeException(String arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public SetAttributeException(ErrorCode arg0, String arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 */
	public SetAttributeException(ErrorCode arg0, String arg1, Throwable arg2) {
		super(arg0, arg1, arg2);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 */
	public SetAttributeException(Throwable arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

}
