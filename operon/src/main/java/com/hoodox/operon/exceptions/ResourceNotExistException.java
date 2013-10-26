/**
 * 
 */
package com.hoodox.operon.exceptions;

import com.hoodox.commons.exceptions.BaseAppException;
import com.hoodox.commons.exceptions.ErrorCode;

/**
 * Exception is thrown by the ResourceManager interface when a Resource is not found
 * @author huac
 *
 */
public class ResourceNotExistException extends BaseAppException {

	private static final long serialVersionUID = -4592360794657955948L;

	/**
	 * @param arg0
	 */
	public ResourceNotExistException(String arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public ResourceNotExistException(ErrorCode arg0, String arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 */
	public ResourceNotExistException(ErrorCode arg0, String arg1, Throwable arg2) {
		super(arg0, arg1, arg2);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 */
	public ResourceNotExistException(Throwable arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

}
