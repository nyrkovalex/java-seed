package com.github.nyrkovalex.seed.test;

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
         * @see org.junit.Assert#assertThat(String, Object, org.hamcrest.Matcher)
         */
        public static void assertThat(boolean condition) {
            Assert.assertThat(condition, is(true));
        }

        @Before
        public void initMocks() {
            MockitoAnnotations.initMocks(this);
        }
    }
}
