package com.github.nyrkovalex.seed.core.chain;


import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * <p>Continuous process abstraction. Chain represents a sequence of {@link Chain.Step}s executed in the FIFO order
 * only if previous step succeeded i.e. no Exception was thrown.</p>
 * <p>
 * <p>{@link Chain.Step}s are considered unrelated and share no context.</p>
 * <p>
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
@SuppressWarnings("UnusedDeclaration")
public class Chain {
    private static final Logger LOG = Logger.getLogger(Chain.class.getName());

    private final List<StepDescription> steps;

    private Chain(Step firstStep, String description) {
        steps = new ArrayList<>();
        addStep(firstStep, description);
    }

    /**
     * Starts a chain construction with a given step
     *
     * @param firstStep   starting step
     * @param description step description
     * @return {@link Chain} being constructed
     */
    public static Chain start(Step firstStep, String description) {
        return new Chain(firstStep, description);
    }

    /**
     * Adds a step to the chain
     *
     * @param nextStep    following step
     * @param description step description
     * @return {@link Chain} being constructed
     */
    public Chain then(Step nextStep, String description) {
        addStep(nextStep, description);
        return this;
    }

    private void addStep(Step step, String description) {
        steps.add(new StepDescription(step, description));
    }

    /**
     * Finishes a chain construction with a given step and runs the chain.
     *
     * @param lastStep    finishing step
     * @param description step description
     * @throws ChainInterruptedException if some step fails. See the cause for details
     */
    public void end(Step lastStep, String description) throws ChainInterruptedException {
        LOG.fine(() -> "Starting chain");
        addStep(lastStep, description);
        for (StepDescription s : steps) {
            try {
                LOG.fine(() -> "Running step " + s.description);
                s.step.run();
            } catch (Exception e) {
                throw new ChainInterruptedException(s.description, e);
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
        void run() throws Exception;
    }

    private static class StepDescription {
        private final Step step;
        private final String description;

        private StepDescription(Step step, String description) {
            this.step = step;
            this.description = description;
        }
    }
}
