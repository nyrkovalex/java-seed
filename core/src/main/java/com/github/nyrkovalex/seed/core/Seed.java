package com.github.nyrkovalex.seed.core;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

@SuppressWarnings("UnusedDeclaration")
public final class Seed {
    private Seed() {}

    public static final class Logging {
        private Logging() {}

        /**
         * Initializes root {@link Logger} and its {@link Handler}s depending on argument provided and
         * {@link com.github.nyrkovalex.seed.core.Seed.Logging.DetailedFormatter}
         *
         * @param debugEnabled whether log debug statements (lower than {@link Level#INFO} or not
         * @param clazz        apply such settings to target class' package and its children
         */
        public static void init(boolean debugEnabled, Class<?> clazz) {
            init(debugEnabled, clazz.getPackage());
        }

        /**
         * Initializes root {@link Logger} and its {@link Handler}s depending on argument provided and
         * {@link com.github.nyrkovalex.seed.core.Seed.Logging.DetailedFormatter}
         *
         * @param debugEnabled whether log debug statements (lower than {@link Level#INFO} or not
         * @param rootPackage  apply such settings to target package and its children
         */
        public static void init(boolean debugEnabled, Package rootPackage) {
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
            init(debugEnabled, clazz.getPackage(), formatter);
        }

        /**
         * Initializes root {@link Logger} and its {@link Handler}s depending on argument provided
         *
         * @param debugEnabled whether log debug statements (lower than {@link Level#INFO} or not
         * @param rootPackage  apply such settings to target package and its children
         * @param formatter    formatter to be used for all loggers
         */
        public static void init(boolean debugEnabled, Package rootPackage, Formatter formatter) {
            Level targetLevel = debugEnabled ? Level.FINEST : Level.INFO;
            Logger rootLogger = Logger.getLogger("");
            for (Handler h : rootLogger.getHandlers()) {
                h.setLevel(targetLevel);
                h.setFormatter(formatter);
            }
            Logger gitdepsLogger = Logger.getLogger(rootPackage.getName());
            gitdepsLogger.setLevel(targetLevel);
        }

        /**
         * Formats detailed log record as follows: <code>date [ LEVEL ] - class.Name: time</code>
         */
        public static class DetailedFormatter extends Formatter {
            @Override
            public String format(LogRecord record) {
                return String.format(
                        "%s [ %s ] - %s: %s\n",
                        new SimpleDateFormat("YYYY-MM-dd HH:mm:ss:SSS").format(
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

    public static final class Files {
        private Files() {
        }

        /**
         * Shorthand for the <code>Files.exists(Paths.get(path))</code>
         *
         * @param path path to check
         * @return <code>true</code> if file or directory exists
         */
        public static boolean exists(String path) {
            return java.nio.file.Files.exists(Paths.get(path));
        }

        /**
         * Deletes target file or directory with its contents, just like <code>rm -rf</code> would
         *
         * @param path file or directory path to delete
         * @throws IOException if something goes wrong
         */
        public static void deleteWithContents(String path) throws IOException {
            try {
                recurseDelete(Paths.get(path));
            } catch (RuntimeException ex) {
                throw new IOException(ex.getCause());
            }
        }

        private static void recurseDelete(Path f) throws RuntimeException {
            try {
                if (java.nio.file.Files.isDirectory(f)) {
                    java.nio.file.Files.list(f).forEach(Files::recurseDelete);
                }
                java.nio.file.Files.deleteIfExists(f);
            } catch (IOException ex) {
                // Propagate as runtime exception so we can use method reference above
                throw new RuntimeException(ex);
            }
        }
    }

    public static final class Console {
        private Console() {
        }

        /**
         * Console input abstraction, useful for mocking this as a dependency
         */
        public static class InputProvider {
            public String read(String prompt) {
                return System.console().readLine(prompt);
            }

            public String readSecure(String prompt) {
                return String.copyValueOf(System.console().readPassword(prompt));
            }
        }
    }

    public static final class Strings {
        private Strings() {
        }

        /**
         * Joins items with provided separator.
         *
         * @param separator separator symbol
         * @param items     items to join
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
    }
}
