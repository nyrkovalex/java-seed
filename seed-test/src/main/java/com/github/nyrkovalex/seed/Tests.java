package com.github.nyrkovalex.seed;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public final class Tests {
	// This class is factored out to standalone module to be used as a dependency with `test` scope

	private Tests() {
	}

	/**
	 * This class is used as a base for all unit tests
	 */
	public static abstract class Expect {

		/**
		 * Expect something to satisfy condition provided to {@link Expectation}
		 *
		 * @param <T>    type of an object under test
		 * @param actual actual value
		 * @return expectation object containing check methods
		 */
		public static <T> Expectation<T> expect(T actual) {
			return new Expectation<>(actual);
		}

		public static <T> Assurance<T> given(T methodCall) {
			return new Assurance<>(methodCall);
		}

		@Before
		public void initMocks() {
			MockitoAnnotations.initMocks(this);
		}
	}

	public static class Assurance<T> {

		private final T methodCall;

		public Assurance(T methodCall) {
			this.methodCall = methodCall;
		}

		public void returns(T value) {
			Mockito.when(methodCall).thenReturn(value);
		}

		public <E extends Throwable> void failsWith(E err) {
			Mockito.when(methodCall).thenThrow(err);
		}

		public <E extends Throwable> void failsWith(Class<E> errClass) {
			Mockito.when(methodCall).thenThrow(errClass);
		}
	}

	/**
	 * This class contains various check methods used in unit testing
	 *
	 * @param <T>
	 */
	public static class Expectation<T> {

		private final T actual;

		private Expectation(T actual) {
			this.actual = actual;
		}

		/**
		 * Checks an equality of an actual item provided earlier and expected argument
		 *
		 * @param expected expected value
		 */
		public void toBe(T expected) {
			Assert.assertThat(actual, CoreMatchers.is(expected));
		}

		/**
		 * Checks that mock had calls to a specific method
		 *
		 * @return mock object itself
		 */
		public T toHaveCall() {
			return Mockito.verify(actual);
		}

		/**
		 * Checks that mock had some number of calls to a specific method
		 *
		 * @param times times a method is expected to be called
		 * @return mock object itself
		 */
		public T toHaveCalls(int times) {
			return Mockito.verify(actual, Mockito.times(times));
		}
	}
}
