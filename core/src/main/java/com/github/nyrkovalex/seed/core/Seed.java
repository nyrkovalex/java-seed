package com.github.nyrkovalex.seed.core;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.stream.Stream;

@SuppressWarnings("UnusedDeclaration")
public final class Seed {

    private Seed() {
    }

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

    public static final class Files {

        private Files() {
        }

        /**
         * Reads target file contents to a single String
         *
         * @param path target file path
         * @return target file content as a single String
         * @throws IOException if something goes wrong
         */
        public static String readToString(String path) throws IOException {
            return new String(java.nio.file.Files.readAllBytes(Paths.get(path)));
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

    /**
     * Console abstraction, useful for mocking this out as a dependency
     */
    public static class Console {

        private Console() {

        }

        public String read(String prompt) {
            return System.console().readLine(prompt);
        }

        public String readSecure(String prompt) {
            return String.copyValueOf(System.console().readPassword(prompt));
        }

        public void printf(String message, Object... args) {
            try (PrintWriter writer = System.console().writer()) {
                writer.printf(message, args);
            }
        }
    }

    /**
     * Creates real {@link Console} object delegating its call to {@link System#console()}
     *
     * @return {@link Console instance}
     */
    public static Console console() {
        return new Console();
    }

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
    }

    /**
     * Helps to load jars from target directory, e.g. useful for plugin implementation
     */
    public static class ClassLoaderProvider {

        private final String path;

        private ClassLoaderProvider(String path) {
            this.path = path;
        }

        private static URL pathToUrl(Path p) throws AssertionError {
            try {
                return p.toUri().toURL();
            } catch (MalformedURLException ex) {
                throw new AssertionError("Should not happen");
            }
        }

        private static List<URL> readDirectory(Path directory) {
            List<URL> urls = new ArrayList<>();
            safeList(directory).forEach(p -> {
                if (!java.nio.file.Files.isDirectory(p)) {
                    urls.add(pathToUrl(p));
                    return;
                }
                urls.addAll(readDirectory(p));

            });
            return urls;
        }

        private static Stream<Path> safeList(Path directory) throws IllegalStateException {
            try {
                return java.nio.file.Files.list(directory);
            } catch (IOException ex) {
                throw new IllegalStateException("Failed to read directory", ex);
            }
        }

        /**
         * Creates a {@link com.github.nyrkovalex.seed.core.Seed.ClassLoaderProvider} for a given
         * path.
         *
         * @param path directory to load classes from
         * @return {@link com.github.nyrkovalex.seed.core.Seed.ClassLoaderProvider} capable of
         * loading classes from a <code>path</code> provided
         */
        public static ClassLoaderProvider forPath(String path) {
            return new ClassLoaderProvider(path);
        }

        /**
         * Creates a {@link java.lang.ClassLoader} recursively scanning <code>path</code> directory
         * contents adding all of the underlying files to its classpath.
         *
         * @return {@link java.lang.ClassLoader} for a given directory
         */
        public ClassLoader get() {
            List<URL> plugins = readDirectory(Paths.get(path));
            return new URLClassLoader(plugins.toArray(new URL[plugins.size()]));
        }
    }
}
