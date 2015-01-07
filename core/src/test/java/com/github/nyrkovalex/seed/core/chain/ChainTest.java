package com.github.nyrkovalex.seed.core.chain;

import com.github.nyrkovalex.seed.test.Seed;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class ChainTest extends Seed.Test {

    @Mock private Chain.Step firstStep;
    @Mock private Chain.Step secondStep;
    @Mock private Chain.Step thirdStep;

    @Test
    public void testShouldExecuteOneStepChain() throws Exception {
        Chain.start(firstStep).end();
        verify(firstStep).call();
    }

    @Test
    public void testShouldExecuteMultipleStepsInTheCorrectOrder() throws Exception {
        Chain.start(firstStep)
                .then(secondStep)
                .then(thirdStep)
                .end();
        InOrder order = inOrder(firstStep, secondStep, thirdStep);
        order.verify(firstStep).call();
        order.verify(secondStep).call();
        order.verify(thirdStep).call();
    }

    @Test
    public void testShouldBreakTheChainOnFailure() throws Exception {
        doThrow(new Exception()).when(secondStep).call();
        try {
            Chain.start(firstStep)
                    .then(secondStep)
                    .then(thirdStep)
                    .end();
        } catch (Exception e) {
            // Fallthrough
        }
        verify(thirdStep, never()).call();
    }

    @Test(expected = Chain.BrokenException.class)
    public void testShouldThrowChainBrokenExceptionOnStepFailure() throws Exception {
        doThrow(new Exception()).when(secondStep).call();
        Chain.start(firstStep)
                .then(secondStep)
                .then(thirdStep)
                .end();
    }
}
