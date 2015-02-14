package com.github.nyrkovalex.seed.test;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.Before;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.OngoingStubbing;
import org.mockito.verification.VerificationMode;


@SuppressWarnings("UnusedDeclaration")
public final class Seed {
    // This class is factored out to standalone module to be used as a dependency with `test` scope

    private Seed() {
    }

    /**
     * This class is used as a base for all unit tests
     */
    public static abstract class Test {
        /**
         * Shorthand for <code>assertThat(condition, is(true));</code>
         *
         * @param condition assertion to verify
         * @see org.junit.Assert#assertThat(Object, org.hamcrest.Matcher)
         */
        public static void assertThat(boolean condition) {
            Assert.assertThat(condition, is(true));
        }

        /**
         * Delegates directly to the {@link org.junit.Assert#assertThat(Object, org.hamcrest.Matcher)}
         * so you won't have to import it for each test
         *
         * @see org.junit.Assert#assertThat(Object, org.hamcrest.Matcher)
         */
        public static <T> void assertThat(T actual, Matcher<? super T> matcher) {
            Assert.assertThat(actual, matcher);
        }


        /**
         * Delegates directly to the {@link org.junit.Assert#assertThat(String, Object, org.hamcrest.Matcher)}
         * so you won't have to import it for each test
         *
         * @see org.junit.Assert#assertThat(String, Object, org.hamcrest.Matcher)
         */
        public static <T> void assertThat(String reason, T actual, Matcher<? super T> matcher) {
            Assert.assertThat(reason, actual, matcher);
        }

        /**
         * Delegates to {@link org.hamcrest.CoreMatchers#is(Object)}.
         *
         * @see org.hamcrest.CoreMatchers#is(java.lang.Object)
         */
        public static <T> Matcher<T> is(T value) {
            return CoreMatchers.is(value);
        }

        /**
         * Delegates to {@link org.hamcrest.CoreMatchers#is(org.hamcrest.Matcher)}.
         *
         * @see org.hamcrest.CoreMatchers#is(org.hamcrest.Matcher)
         */
        public static <T> Matcher<T> is(Matcher<T> matcher) {
            return CoreMatchers.is(matcher);
        }

        /**
         * Delegates to {@link org.hamcrest.CoreMatchers#sameInstance(Object)}
         *
         * @see org.hamcrest.CoreMatchers#sameInstance(Object)
         */
        public static <T> Matcher<T> sameInstance(T target) {
            return CoreMatchers.sameInstance(target);
        }

        /**
         * Delegates to {@link org.hamcrest.CoreMatchers#instanceOf(Class)}
         *
         * @see org.hamcrest.CoreMatchers#instanceOf(Class)
         */
        public static <T> Matcher<T> instanceOf(Class<?> type) {
            return CoreMatchers.instanceOf(type);
        }
        
        /**
         * Delegates to {@link Mockito#verify(java.lang.Object) }
         * 
         * @see Mockito#verify(java.lang.Object)
         */
        public static <T> T verify(T mock) {
            return Mockito.verify(mock);
        }
        
        /**
         * Delegates to {@link Mockito#verify(java.lang.Object, VerificationMode) }
         * 
         * @see Mockito#verify(java.lang.Object, VerificationMode)
         */
        public static <T> T verify(T mock, VerificationMode mode) {
            return Mockito.verify(mock, mode);
        }
        
        /**
         * Delegates to {@link Mockito#when(java.lang.Object) }
         * 
         * @see Mockito#when(java.lang.Object) 
         */
        public static <T> OngoingStubbing<T> when(T methodCall) {
            return Mockito.when(methodCall);
        }

        @Before
        public void initMocks() {
            MockitoAnnotations.initMocks(this);
        }
    }
}
