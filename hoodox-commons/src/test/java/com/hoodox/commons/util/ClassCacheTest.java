package com.hoodox.commons.util;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.testng.annotations.Test;

import com.hoodox.commons.exceptions.BaseSystemException;

public class ClassCacheTest {

	public static class First {
	}

	public static class Second {
	}

	/** Only used by the threads test, so no other test has cached it. */
	public static class SlowToCreate {
		public SlowToCreate() throws InterruptedException {
			Thread.sleep(50);
		}
	}

	@Test
	public void sameInstanceOnTwoCalls() {
		Object one = ClassCache.getInstance().getObject(First.class.getName());
		Object two = ClassCache.getInstance().getObject(First.class.getName());

		assertTrue(one instanceof First);
		assertSame(two, one);
	}

	@Test
	public void distinctClassesGiveDistinctObjects() {
		Object first = ClassCache.getInstance().getObject(First.class.getName());
		Object second = ClassCache.getInstance().getObject(Second.class.getName());

		assertTrue(second instanceof Second);
		assertNotSame(second, first);
	}

	@Test(expectedExceptions = BaseSystemException.class)
	public void unknownClassIsSystemException() {
		ClassCache.getInstance().getObject("com.hoodox.commons.util.NoSuchClass");
	}

	@Test
	public void threadsAskingAtOnceGetOneInstance() throws Exception {
		final int threads = 8;
		final CountDownLatch ready = new CountDownLatch(threads);
		ExecutorService executor = Executors.newFixedThreadPool(threads);
		try {
			List<Future<Object>> results = new ArrayList<Future<Object>>();
			for (int i = 0; i < threads; i++) {
				results.add(executor.submit(new Callable<Object>() {
					public Object call() throws Exception {
						ready.countDown();
						ready.await();
						return ClassCache.getInstance().getObject(SlowToCreate.class.getName());
					}
				}));
			}

			Object expected = results.get(0).get();
			assertEquals(results.size(), threads);
			for (Future<Object> result : results) {
				assertSame(result.get(), expected);
			}
		} finally {
			executor.shutdown();
		}
	}
}
