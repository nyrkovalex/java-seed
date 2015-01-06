package com.github.nyrkovalex.seed.test;

import org.junit.Before;
import org.mockito.MockitoAnnotations;


@SuppressWarnings("UnusedDeclaration")
public final class Seed {
    private Seed() {}

    public static abstract class Test {
        @Before
        public void initMocks() {
            MockitoAnnotations.initMocks(this);
        }
    }
}
