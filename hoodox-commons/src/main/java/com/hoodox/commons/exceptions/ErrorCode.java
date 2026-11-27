package com.hoodox.commons.exceptions;

import java.io.Serializable;

/**
 * The code that identifies a kind of error.
 */
public class ErrorCode implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String code;

	public ErrorCode(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}
