package com.hoodox.commons.exceptions;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

public class BaseExceptionsTest {

	private final ErrorCode code = new ErrorCode("some_error");
	private final Throwable cause = new IllegalStateException("root cause");

	@Test
	public void errorCodeReturnsItsString() {
		assertEquals(code.getCode(), "some_error");
	}

	@Test
	public void systemExceptionWithMessage() {
		BaseSystemException ex = new BaseSystemException("message");
		check(ex, ex, "message", null, null);
	}

	@Test
	public void systemExceptionWithCodeAndMessage() {
		BaseSystemException ex = new BaseSystemException(code, "message");
		check(ex, ex, "message", null, "some_error");
	}

	@Test
	public void systemExceptionWithCodeMessageAndCause() {
		BaseSystemException ex = new BaseSystemException(code, "message", cause);
		check(ex, ex, "message", cause, "some_error");
	}

	@Test
	public void systemExceptionWithCause() {
		BaseSystemException ex = new BaseSystemException(cause);
		check(ex, ex, cause.toString(), cause, null);
	}

	@Test
	public void appExceptionWithMessage() {
		BaseAppException ex = new BaseAppException("message");
		check(ex, ex, "message", null, null);
	}

	@Test
	public void appExceptionWithCodeAndMessage() {
		BaseAppException ex = new BaseAppException(code, "message");
		check(ex, ex, "message", null, "some_error");
	}

	@Test
	public void appExceptionWithCodeMessageAndCause() {
		BaseAppException ex = new BaseAppException(code, "message", cause);
		check(ex, ex, "message", cause, "some_error");
	}

	@Test
	public void appExceptionWithCause() {
		BaseAppException ex = new BaseAppException(cause);
		check(ex, ex, cause.toString(), cause, null);
	}

	@Test
	public void notLoggedUntilSetLogged() {
		IBaseException[] exceptions = { new BaseSystemException("system"), new BaseAppException("app") };
		for (IBaseException ex : exceptions) {
			assertFalse(ex.isLogged());
			ex.setLogged();
			assertTrue(ex.isLogged());
		}
	}

	@Test
	public void checkedAndUncheckedTypes() {
		Object system = new BaseSystemException("system");
		Object app = new BaseAppException("app");
		Object xml = new XMLAccessException("xml");

		assertTrue(system instanceof RuntimeException);
		assertFalse(app instanceof RuntimeException);
		assertTrue(app instanceof Exception);
		assertTrue(xml instanceof BaseAppException);
		assertFalse(xml instanceof java.io.IOException);
	}

	private static void check(IBaseException ex, Throwable throwable, String message, Throwable expectedCause,
			String errorCode) {
		assertEquals(ex.getMessage(), message);
		assertSame(throwable.getCause(), expectedCause);
		if (errorCode == null) {
			assertNull(ex.getErrorCode());
		} else {
			assertEquals(ex.getErrorCode(), errorCode);
		}
	}
}
