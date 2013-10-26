package com.hoodox.operon.exceptions;

import com.hoodox.commons.exceptions.BaseSystemException;
import com.hoodox.commons.exceptions.ErrorCode;

/**
 * This is thrown when we try to update something that has out of syn lockversions in the database
 * @author huac
 *
 */
public class DaoLockVersionException extends BaseSystemException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7245109944498609719L;

	public DaoLockVersionException(String arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public DaoLockVersionException(ErrorCode arg0, String arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}

	public DaoLockVersionException(ErrorCode arg0, String arg1, Throwable arg2) {
		super(arg0, arg1, arg2);
		// TODO Auto-generated constructor stub
	}

	public DaoLockVersionException(Throwable arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

}
