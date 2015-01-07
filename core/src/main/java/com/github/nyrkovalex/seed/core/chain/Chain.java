package com.github.nyrkovalex.seed.core.chain;


import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * <p>Continuous process abstraction. Chain represents a sequence of {@link Chain.Step}s executed in the FIFO order
 * only if previous step succeeded i.e. no Exception was thrown.</p>
 * <p>{@link Chain.Step}s are considered unrelated and share no context.</p>
 * <pre>
 *     // Example usage:
 *
 *     Chain.start(() -> first.thing(), "first step")
 *           .then(() -> second.thing(), "second step")
 *            .end(() -> last.thing(), "last step");
 * </pre>
 *
 * @see Chain.Step
 */
public class Chain {
    private static final Logger LOG = Logger.getLogger(Chain.class.getName());

    private final List<Step> steps;

    private Chain(Step firstStep) {
        steps = new ArrayList<>();
        addStep(firstStep);
    }

    /**
     * Starts a chain construction with a given step
     *
     * @param firstStep starting step
     * @return {@link Chain} being constructed
     */
    public static Chain start(Step firstStep) {
        return new Chain(firstStep);
    }

    /**
     * Adds a step to the chain
     *
     * @param nextStep following step
     * @return {@link Chain} being constructed
     */
    public Chain then(Step nextStep) {
        addStep(nextStep);
        return this;
    }

    private void addStep(Step step) {
        steps.add(step);
    }

    /**
     * Finishes a chain construction and runs the chain.
     *
     * @throws Chain.BrokenException if some step fails. See the cause for details
     */
    public void end() throws Chain.BrokenException {
        LOG.fine(() -> "Starting chain");
        for (Step s : steps) {
            try {
                s.call();
            } catch (Exception e) {
                throw new Chain.BrokenException(s, e);
            }
        }
        LOG.fine(() -> "Chain successfully completed");
    }

    /**
     * {@link Chain} step. To be used as lambda
     *
     * @see Chain
     */
    @FunctionalInterface
    public static interface Step {

        /**
         * Does the job and throws if something goes wrong
         *
         * @throws Exception if something goes wrong which interrupts the chain
         */
        void call() throws Exception;
    }

    public static class BrokenException extends Exception {
        private BrokenException(Step s, Throwable cause) {
            super(String.format("Chain broken. Step %s failed", s), cause);
        }
    }
}
