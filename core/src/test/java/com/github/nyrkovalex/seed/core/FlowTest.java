package com.github.nyrkovalex.seed.core;

import com.github.nyrkovalex.seed.test.Seed;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FlowTest extends Seed.Test {

    private static final String RESULT = "result";
    @Mock private Flow.Processor<Integer, String> step;
    @Mock private Flow.Processor<Integer, Integer> brokenStep;

    @Before
    public void setUp() throws Exception {
        when(brokenStep.apply(anyInt())).thenThrow(new Exception());
    }

    @Test
    public void testShouldInvokeOneStep() throws Exception {
        String result = Flow.start(() -> RESULT).end();
        expect(result).toBe(RESULT);
    }

    @Test
    public void testShouldInvokeMultipleSteps() throws Exception {
        String result = Flow
                .start(() -> "1")
                .then((i) -> i.equals("1") ? 2 : 0)
                .then((i) -> i == 2)
                .then((i) -> i ? RESULT : "")
                .end();
        expect(result).toBe(RESULT);
    }

    @Test
    public void testShouldInterruptOnException() throws Exception {
        try {
            Flow.start(() -> 1)
                    .then(brokenStep)
                    .then(step)
                    .end();
        } catch (Exception e) {
            // Fallthrough
        }
        verify(step, never()).apply(anyInt());
    }

    @Test(expected = Flow.InterruptedException.class)
    public void testShouldThrowFlowInterruptedExceptionWhenStepFails() throws Exception {
        Flow.start(() -> 1)
                .then(brokenStep)
                .then(step)
                .end();
    }
}
