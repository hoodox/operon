package com.hoodox.commons.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.hoodox.commons.exceptions.BaseSystemException;
import com.hoodox.commons.exceptions.ErrorCode;

/**
 * Keeps one object per class name, created with the class's no-arg constructor.
 */
public class ClassCache {

	private static final ClassCache INSTANCE = new ClassCache();

	private final ConcurrentMap<String, Object> objects = new ConcurrentHashMap<String, Object>();

	private ClassCache() {
	}

	public static ClassCache getInstance() {
		return INSTANCE;
	}

	/**
	 * @return the object for the class name; every call with the same name
	 *         returns the same object
	 * @throws BaseSystemException if the object cannot be created
	 */
	public Object getObject(String className) {
		Object object = objects.get(className);
		if (object != null) {
			return object;
		}
		// one lock for creating, so each class is created once even when threads ask at
		// the same time (re-entrant: a constructor may itself call getObject)
		synchronized (this) {
			object = objects.get(className);
			if (object == null) {
				object = create(className);
				objects.put(className, object);
			}
			return object;
		}
	}

	private static Object create(String className) {
		try {
			return Class.forName(className, true, Thread.currentThread().getContextClassLoader())
					.getDeclaredConstructor().newInstance();
		} catch (Exception e) {
			throw cannotCreate(className, e);
		} catch (LinkageError e) {
			throw cannotCreate(className, e);
		}
	}

	private static BaseSystemException cannotCreate(String className, Throwable cause) {
		return new BaseSystemException(new ErrorCode("class_cache_error"), "Cannot create " + className, cause);
	}
}
