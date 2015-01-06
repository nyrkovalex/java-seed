package com.github.nyrkovalex.seed.core.flow;


import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * <p>Continuous process abstraction. Flow represents a sequence of {@link Flow.Step}s executed in the FIFO order
 * only if previous step succeeded i.e. no Exception was thrown.</p>
 * <p>
 * <p>{@link Flow.Step}s are considered unrelated and share no context.</p>
 * <p>
 * <pre>
 *     // Example usage:
 *
 *     Flow.start(() -> first.thing(), "first step")
 *          .then(() -> second.thing(), "second step")
 *           .end(() -> last.thing(), "last step");
 * </pre>
 *
 * @see com.github.nyrkovalex.seed.core.flow.Flow.Step
 */
@SuppressWarnings("UnusedDeclaration")
public class Flow {
    private static final Logger LOG = Logger.getLogger(Flow.class.getName());

    private final List<StepDescription> steps;

    private Flow(Step firstStep, String description) {
        steps = new ArrayList<>();
        addStep(firstStep, description);
    }

    /**
     * Starts a flow construction with a given step
     *
     * @param firstStep   starting step
     * @param description step description
     * @return {@link Flow} being constructed
     */
    public static Flow start(Step firstStep, String description) {
        return new Flow(firstStep, description);
    }

    /**
     * Adds a step to the flow
     *
     * @param nextStep    following step
     * @param description step description
     * @return {@link Flow} being constructed
     */
    public Flow then(Step nextStep, String description) {
        addStep(nextStep, description);
        return this;
    }

    private void addStep(Step step, String description) {
        steps.add(new StepDescription(step, description));
    }

    /**
     * Finishes a flow construction with a given step and runs the flow.
     *
     * @param lastStep    finishing step
     * @param description step description
     * @throws com.github.nyrkovalex.seed.core.flow.FlowInterruptedException if some step fails. See the cause for details
     */
    public void end(Step lastStep, String description) throws FlowInterruptedException {
        LOG.fine(() -> "Starting flow");
        addStep(lastStep, description);
        for (StepDescription s : steps) {
            try {
                LOG.fine(() -> "Running step " + s.description);
                s.step.run();
            } catch (Exception e) {
                throw new FlowInterruptedException(s.description, e);
            }
        }
        LOG.fine(() -> "Flow successfully completed");
    }

    /**
     * {@link Flow} step. To be used as lambda
     *
     * @see com.github.nyrkovalex.seed.core.flow.Flow
     */
    @FunctionalInterface
    public static interface Step {

        /**
         * Does the job and throws if something goes wrong
         *
         * @throws Exception if something goes wrong which interrupts the flow
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
