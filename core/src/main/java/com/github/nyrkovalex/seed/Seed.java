package com.github.nyrkovalex.seed;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

@SuppressWarnings("UnusedDeclaration")
public final class Seed {

    /**
     * Creates a {@link Logger} for a given {@link Class} using its name as a {@link Logger} name
     *
     * @param clazz class to create a {@link Logger} for
     * @return {@link Logger} for a given class
     *
     * @see Logger
     */
    public static Logger logger(Class<?> clazz) {
        return Logger.getLogger(clazz.getName());
    }

    /**
     * Creates an {@link Seed.Error} instance for capturing exceptions of type <code>T</code>
     *
     * @param <T> type of exception this error object will catch
     * @param errClass class of an expected exception
     * @return {@link Seed.Error} capable of catching exceptions of type <code>T</code>
     */
    public static <T extends Throwable> Error<T> error(Class<T> errClass) {
        return new SeedError<>(errClass);
    }

    private Seed() {
        // Module
    }


    /**
     * Contains helping functions for logging jumpstart using {@link Logger}
     */
    public static final class Logging {

        private Logging() {
        }

        /**
         * Initializes root {@link Logger} and its {@link Handler}s depending on argument provided
         * and {@link com.github.nyrkovalex.seed.core.Seed.Logging.DetailedFormatter}
         *
         * @param debugEnabled whether log debug statements (lower than {@link Level#INFO} or not
         * @param clazz apply such settings to target class' package and its children
         */
        public static void init(boolean debugEnabled, Class<?> clazz) {
            init(debugEnabled, clazz.getPackage().getName());
        }

        /**
         * Initializes root {@link Logger} and its {@link Handler}s depending on argument provided
         * and {@link com.github.nyrkovalex.seed.core.Seed.Logging.DetailedFormatter}
         *
         * @param debugEnabled whether log debug statements (lower than {@link Level#INFO} or not
         * @param rootPackage apply such settings to target package and its children
         */
        public static void init(boolean debugEnabled, String rootPackage) {
            init(debugEnabled, rootPackage, new DetailedFormatter());
        }

        /**
         * Initializes root {@link Logger} and its {@link Handler}s depending on argument provided
         *
         * @param debugEnabled whether log debug statements (lower than {@link Level#INFO} or not
         * @param clazz apply such settings to target class' package and its children
         * @param formatter formatter to be used for all loggers
         */
        public static void init(boolean debugEnabled, Class<?> clazz, Formatter formatter) {
            init(debugEnabled, clazz.getPackage().getName(), formatter);
        }

        /**
         * Initializes root {@link Logger} and its {@link Handler}s depending on argument provided
         *
         * @param debugEnabled whether log debug statements (lower than {@link Level#INFO} or not
         * @param rootPackage apply such settings to target package and its children
         * @param formatter formatter to be used for all loggers
         */
        public static void init(boolean debugEnabled, String rootPackage, Formatter formatter) {
            Level targetLevel = debugEnabled ? Level.FINEST : Level.INFO;
            Logger rootLogger = Logger.getLogger("");
            for (Handler h : rootLogger.getHandlers()) {
                h.setLevel(targetLevel);
                h.setFormatter(formatter);
            }
            Logger logger = Logger.getLogger(rootPackage);
            logger.setLevel(targetLevel);
        }

        /**
         * Formats detailed log record as follows: <code>date [ LEVEL ] - class.Name: time</code>
         */
        public static class DetailedFormatter extends Formatter {

            @Override
            public String format(LogRecord record) {
                return String.format(
                        "%s [ %s ] - %s: %s\n",
                        new SimpleDateFormat("YYYY-MM-dd HH:mm:ss.SSS").format(
                                new Date(record.getMillis())),
                        record.getLevel(),
                        record.getSourceClassName(),
                        record.getMessage()
                );
            }
        }

        /**
         * Simply writes log message with no details around
         */
        public static class StdOutFormatter extends java.util.logging.Formatter {

            @Override
            public String format(LogRecord record) {
                return record.getMessage() + "\n";
            }
        }
    }

    /**
     * Helper functions for {@link String} class
     */
    public static final class Strings {

        private Strings() {
        }

        /**
         * Joins items with provided separator.
         *
         * @param separator separator symbol
         * @param items items to join
         * @return string representation of items joined with separator
         */
        public static String join(String separator, Iterable<?> items) {
            StringBuilder builder = new StringBuilder();
            boolean appendSeparator = false;
            for (Object item : items) {
                if (appendSeparator) {
                    builder.append(separator);
                }
                builder.append(item);
                appendSeparator = true;
            }
            return builder.toString();
        }

        /**
         * Joins items with provided separator.
         *
         * @param separator separator symbol
         * @param items items to join
         * @return string representation of items joined with separator
         */
        public static String join(String separator, Object... items) {
            return join(separator, Arrays.asList(items));
        }

    }

    /**
     * <p>
     * Wraps an exception instance, can be used to rethrow an exception captured from lambda.
     * </p>
     * <p>
     * Not thread-safe
     * </p>
     *
     * @param <T> type of ann error to catch
     */
    public static interface Error<T extends Throwable> {

        /**
         * Catches a throwable for further propagation.
         *
         * @param err
         * @throws IllegalStateException
         */
        void propagate(T err) throws IllegalStateException;

        /**
         * Throws error captured or does nothing if no error was caught.
         *
         * @throws T if something nasty happenes
         */
        void rethrow() throws T;

        /**
         * <p>
         * Calls an {@link UnsafeCallable} capturing any exception of an expected type and
         * throwing a {@link RuntimeException} wrapping a throwable of any other type if thrown.
         * </p>
         * <p>
         * <i>Don't go suicidal. Never use this to silence an error.</i>
         * </p>
         * @param call
         */
        void safeCall(VoidUnsafeCall call);
    }


	/**
	 * Lambda that can fail with exception returning some output.
	 *
	 * @param <T> type on an output produced by lambda
	 */
	@FunctionalInterface
	public static interface UnsafeCall<T> {

		T call() throws Exception;
	}

	/**
	 * Lambda that can fail with exception doing void call.
	 */
	@FunctionalInterface
	public static interface VoidUnsafeCall {

		void call() throws Exception;
	}
}


class SeedError<T extends Throwable> implements Seed.Error<T> {
    private final Class<T> errClass;
    private Optional<T> err;

    public SeedError(Class<T> errClass) {
        this.errClass = errClass;
        err = Optional.empty();
    }

    @Override
    public void rethrow() throws T {
        if (err.isPresent()) {
            throw err.get();
        }
    }

    @Override
    public void propagate(T err) throws IllegalStateException {
        if (this.err.isPresent()) {
            throw new IllegalStateException(
                    "Error already set, cannot propagate more than one exception");
        }
        this.err = Optional.of(err);
    }

    @Override
    public void safeCall(Seed.VoidUnsafeCall callable) {
        try {
            callable.call();
        } catch (Throwable t) {
            if (!errClass.isInstance(t)) {
                throw new RuntimeException(t);
            }
            propagate(errClass.cast(t));
        }
    }
}
