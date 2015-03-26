package com.github.nyrkovalex.seed;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;
import java.util.function.Function;

public final class Io {
    private Io() {
    }

    /**
     * Creates a filesystem absraction that can be used as an external
     * dependency for your classes
     *
     * @return {@link Fs} instance
     */
    public static Fs fs() {
        return IoFs.instance();
    }

    /**
     * Easy to use file abstraction suitable for mocking
     */
    public static interface File {

        /**
         * Deletes target file or directory with its contents, just like
         * <code>rm -rf</code> would
         *
         * @throws IOException
         *             if something goes wrong
         */
        void deleteWithContents() throws Io.Err;

        /**
         * Check whether file with a given path exists
         *
         * @return true if file exists on a given path
         */
        boolean exists();

        /**
         * Path current object is bound to
         *
         * @return target path
         */
        String path();

        /**
         * Creates a {@link BufferedReader} reading current file bytes
         *
         * @return {@link BufferedReader} for current file
         * @throws IOException
         *             if something goes wrong
         */
        BufferedReader reader() throws IOException;

        <T> T reader(Function<BufferedReader, T> handler) throws Io.Err;

        /**
         * Reads current file as an {@link InputStream}
         *
         * @return {@link InputStream} of a current file
         * @throws IOException
         *             if something goes wrong
         */
        InputStream stream() throws Io.Err;

        <T> T stream(Function<InputStream, T> handler) throws Io.Err;

        /**
         * Reads target file contents to a single String
         *
         * @return target file content as a single String
         * @throws IOException
         *             if something goes wrong
         */
        String string() throws Io.Err;

        /**
         * Writes bytes to a current file
         *
         * @param data
         *            bytes to write
         * @throws IOException
         *             if something goes wrong
         */
        void write(byte[] data) throws Io.Err;

        void write(Consumer<BufferedWriter> handler) throws Io.Err;
    }

    /**
     * Filesystem abstraction mostly for dependency injection and easy mocking
     */
    public static interface Fs {

        /**
         * Creates a {@link Seed.File} instance on a given path
         *
         * @param path
         *            path to a file
         * @return {@link Seed.File} instance bound to a given path
         */
        File file(String path);
    }

    public static class Err extends Exception {
        private static final long serialVersionUID = 1L;

        Err(Throwable cause) {
            super(cause);
        }

        public static void rethrow(Object object) {
            // TODO Auto-generated method stub

        }

        static void rethrow(VoidUnsafeCall call) throws Io.Err {
            try {
                call.call();
            } catch (Exception ex) {
                throw from(ex);
            }
        }

        static Io.Err from(Exception ex) {
            if (ex instanceof Io.Err)
                return (Io.Err) ex;
            return new Io.Err(ex);
        }

        static <T> T rethrow(UnsafeCall<T> call) throws Io.Err {
            try {
                return call.call();
            } catch (Exception ex) {
                throw from(ex);
            }
        }

        @FunctionalInterface
        static interface UnsafeCall<T> {
            T call() throws Exception;
        }

        @FunctionalInterface
        static interface VoidUnsafeCall {
            void call() throws Exception;
        }
    }

}
