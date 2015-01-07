package com.github.nyrkovalex.seed.core;

/**
 * Continuous process abstraction where each step depends on previous step's output.
 * For a simple sequential invocation when no data is shared between steps see
 * {@link com.github.nyrkovalex.seed.core.Chain Chain}.
 * <pre>
 * // Example usage
 *
 * String result = Flow
 *         .start(() -> "1")                   // return string "1"
 *         .then((i) -> i.equals("1") ? 2 : 0) // if string "1" is returned by previous step, return int 2
 *         .then((i) -> i == 2)                // if int 2 returned by previous step return true
 *         .then((i) -> i ? "Done!" : "")      // if true was returned by previous step return string "Done!"
 *         .end();
 *
 * // result.equals("Done!")
 * </pre>
 *
 * @see com.github.nyrkovalex.seed.core.Chain Chain
 * @see Flow.FirstStep
 * @see Flow.PendingStep
 * @see Flow.Invocation
 */
public class Flow {

    /**
     * Starts a new flow return {@link Flow.Invocation} object. Think of it as a
     * deferred function call where function is the {@link Flow.FirstStep}
     * provided.
     *
     * @param step step to begin Flow with
     * @param <O>  type of a result returned by {@link Flow.FirstStep#call()}
     * @return {@link Flow.Invocation}
     * object representing continuous flow construction
     */
    public static <O> Invocation<O> start(FirstStep<O> step) {
        return new FirstInvocation<>(step);
    }

    /**
     * <p>Pending step to be executed in the middle or at te end of a {@link Flow}.
     * Can be used as a lambda function.</p>
     * <p>To get more detailed exception output one may want to override its {@link #toString()} method which will be used
     * in the {@link Flow.InterruptedException} message.</p>
     *
     * @param <I> type of a parameter expected by this step's
     *            {@link Flow.PendingStep#apply(I)} method
     * @param <O> type of result returned by {@link Flow.PendingStep#apply(I)}
     */
    @FunctionalInterface
    public static interface PendingStep<I, O> {

        /**
         * Performes some actions on <code>data</code> relieved from upstream step and produces output for
         * downstream steps.
         *
         * @param data data provided by an upstream step
         * @return output for a downstream step
         * @throws Exception if something goes wrong. This exception interrupts the flow
         */
        O apply(I data) throws Exception;
    }

    /**
     * <p>First step to start {@link Flow} with. Expects no input,
     * serves just to produce initial data.
     * Can be used as a lambda function.</p>
     * <p>To get more detailed exception output one may want to override its {@link #toString()} method which will be used
     * in the {@link Flow.InterruptedException} message.</p>
     *
     * @param <O> type of a result returned by {@link Flow.FirstStep#call()}
     */
    @FunctionalInterface
    protected static interface FirstStep<O> {

        /**
         * Produces output for a downstream step.
         *
         * @return data to process further down the flow
         * @throws Exception if something goes wrong. This exception interrupts the flow
         */
        O call() throws Exception;
    }

    private static class PendingInvocation<I, O> extends Invocation<O> {
        private final PendingStep<I, O> step;
        private final Invocation<I> previous;

        public PendingInvocation(Invocation<I> previous, PendingStep<I, O> step) {
            this.step = step;
            this.previous = previous;
        }

        protected O invoke() throws InterruptedException {
            I out = previous.invoke();
            try {
                return step.apply(out);
            } catch (Exception e) {
                throw new InterruptedException(step, e);
            }
        }
    }

    private static class FirstInvocation<O> extends Invocation<O> {

        private final FirstStep<O> step;

        private FirstInvocation(FirstStep<O> step) {
            this.step = step;
        }

        @Override
        protected O invoke() throws InterruptedException {
            try {
                return step.call();
            } catch (Exception e) {
                throw new InterruptedException(step, e);
            }
        }
    }

    /**
     * Represents a deferred invocation of a {@link Flow} step.
     * Servers as a builder for a flow itself.
     *
     * @param <O> type of an output produced by this invocation
     */
    public static abstract class Invocation<O> {
        protected abstract O invoke() throws InterruptedException;

        /**
         * Connects this step to a downstream one producing a new
         * {@link Flow.Invocation} instance connected with itself.
         *
         * @param nextStep next step to execute down the flow
         * @param <N>      type of next step's output
         * @return next {@link Flow.Invocation} down the
         * {@link Flow} stream
         */
        public <N> Invocation<N> then(PendingStep<O, N> nextStep) {
            return new PendingInvocation<>(this, nextStep);
        }

        /**
         * Ends the flow invoking all the steps in the FIFO order passing each step's output to the downstream step
         *
         * @return last {@link Flow} step output
         * @throws Flow.InterruptedException when something goes wrong up in the
         *                                                                        {@link Flow} you'll probably want to check the cause
         */
        public O end() throws Flow.InterruptedException {
            return invoke();
        }
    }

    /**
     * Thrown when one of the {@link Flow} steps fails interrupting the Flow.
     * To get more from the exception message one may want to override
     * {@link Flow.FirstStep#toString()} and
     * {@link Flow.PendingStep#toString()} methods
     */
    public static class InterruptedException extends Exception {
        private InterruptedException(String s, Throwable cause) {
            super("Step " + s + " failed", cause);

        }

        private InterruptedException(FirstStep<?> step, Throwable cause) {
            this(step.toString(), cause);
        }

        private InterruptedException(PendingStep<?, ?> step, Throwable cause) {
            this(step.toString(), cause);
        }
    }
}
