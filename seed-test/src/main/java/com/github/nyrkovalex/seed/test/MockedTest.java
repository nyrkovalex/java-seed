package com.github.nyrkovalex.seed.test;

import org.junit.Before;
import org.mockito.MockitoAnnotations;

/**
 * This class is used as a base for all unit tests
 */
public abstract class MockedTest {
	@Before
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
	}
}
