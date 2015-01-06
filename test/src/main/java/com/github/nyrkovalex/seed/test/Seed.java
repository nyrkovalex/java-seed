package com.github.nyrkovalex.seed.test;

import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.Before;
import org.mockito.MockitoAnnotations;

import static org.hamcrest.CoreMatchers.is;


@SuppressWarnings("UnusedDeclaration")
public final class Seed {
    private Seed() {}

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

        @Before
        public void initMocks() {
            MockitoAnnotations.initMocks(this);
        }
    }
}
