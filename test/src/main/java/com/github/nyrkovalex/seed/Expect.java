package com.github.nyrkovalex.seed;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;


@SuppressWarnings("UnusedDeclaration")
public final class Expect {
    // This class is factored out to standalone module to be used as a dependency with `test` scope

    private Expect() {
    }

    /**
     * This class is used as a base for all unit tests
     */
    public static abstract class Test {

        /**
         * Expect something to satisfy condition provided to {@link Expectation}
         *
         * @param <T> type of an object under test
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
