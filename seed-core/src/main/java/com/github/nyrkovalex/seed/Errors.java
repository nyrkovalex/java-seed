package com.github.nyrkovalex.seed;

import java.util.Optional;

public class Errors {

	private Errors() {
		// Module
	}

	@FunctionalInterface
	public interface ErrProvider<E> {

		E provide(Exception cause);
	}

	public static <T, E extends Throwable> T rethrow(UnsafeCall<T> call, ErrProvider<E> err) throws E {
		try {
			return call.call();
		} catch (Exception ex) {
			throw err.provide(ex);
		}
	}

	public static <E extends Throwable> void rethrow(VoidUnsafeCall call, ErrProvider<E> err) throws E {
		try {
			call.call();
		} catch (Exception ex) {
			throw err.provide(ex);
		}
	}

	/**
	 * Creates an {@link Catcher} instance for capturing exceptions of type <code>T</code>
	 *
	 * @param <T>      type of exception this catcher object will catch
	 * @param errClass class of an expected exception
	 * @return {@link Seed.Catcher} capable of catching exceptions of type <code>T</code>
	 */
	public static <T extends Throwable> Catcher<T> catcher(Class<T> errClass) {
		return new ErrorsCatcher<>(errClass);
	}

	/**
	 * <p>
	 * Wraps an exception instance, can be used to rethrow an exception captured from lambda.
	 * </p>
	 * <p>
	 * Not thread-safe
	 * </p>
	 *
	 * @param <T> type of ann catcher to catch
	 */
	public interface Catcher<T extends Throwable> {

		/**
		 * Catches a throwable for further propagation.
		 *
		 * @param err
		 * @throws IllegalStateException
		 */
		void propagate(T err) throws IllegalStateException;

		/**
		 * Throws catcher captured or does nothing if no catcher was caught.
		 *
		 * @throws T if something nasty happens
		 */
		void rethrow() throws T;

		/**
		 * <p>
		 * Calls an {@link VoidUnsafeCall} capturing any exception of an expected type and throwing a
		 * {@link RuntimeException} wrapping a throwable of any other type if thrown.
		 * </p>
		 * <p>
		 * <i>Don't go suicidal. Never use this to silence an catcher.</i>
		 * </p>
		 *
		 * @param call
		 */
		void safeCall(VoidUnsafeCall call);

		/**
		 * <p>
		 * Calls an {@link UnsafeCall} capturing any exception of an expected type and throwing a
		 * {@link RuntimeException} wrapping a throwable of any other type if thrown.
		 * </p>
		 * <p>
		 * <i>Don't go suicidal. Never use this to silence an catcher.</i>
		 * </p>
		 *
		 * @param <T>  type of a lambda result
		 * @param call lambda to wrap
		 * @return result of <code>call</code> invocation
		 */
		<T> Optional<T> safeCall(UnsafeCall<T> call);
	}

	/**
	 * Lambda that can fail with exception returning some output.
	 *
	 * @param <T> type on an output produced by lambda
	 */
	@FunctionalInterface
	public interface UnsafeCall<T> {

		T call() throws Exception;
	}

	/**
	 * Lambda that can fail with exception doing void call.
	 */
	@FunctionalInterface
	public interface VoidUnsafeCall {

		void call() throws Exception;
	}

	private static class ErrorsCatcher<T extends Throwable> implements Catcher<T> {

		private final Class<T> errClass;
		private Optional<T> err;

		public ErrorsCatcher(Class<T> errClass) {
			this.errClass = errClass;
			err = Optional.empty();
		}

		@Override
		public void rethrow() throws T {
			if (err.isPresent()) {
				throw err.get();
			}
		}

		@Override
		public void propagate(T err) throws IllegalStateException {
			if (this.err.isPresent()) {
				throw new IllegalStateException(
						"Error already set, cannot propagate more than one exception");
			}
			this.err = Optional.of(err);
		}

		@Override
		public void safeCall(VoidUnsafeCall callable) {
			try {
				callable.call();
			} catch (Throwable t) {
				if (!errClass.isInstance(t)) {
					throw new RuntimeException(t);
				}
				propagate(errClass.cast(t));
			}
		}

		@Override
		public <R> Optional<R> safeCall(UnsafeCall<R> call) {
			Optional<R> result = Optional.empty();
			try {
				result = Optional.of(call.call());
			} catch (Throwable t) {
				if (!errClass.isInstance(t)) {
					throw new RuntimeException(t);
				}
				propagate(errClass.cast(t));
			}
			return result;
		}
	}
}
