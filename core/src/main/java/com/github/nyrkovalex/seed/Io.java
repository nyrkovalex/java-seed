package com.github.nyrkovalex.seed;

import com.github.nyrkovalex.seed.Io.Entity;
import com.github.nyrkovalex.seed.Io.Err;
import com.github.nyrkovalex.seed.Io.File;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Io {

    private Io() {
        // Module
    }

    /**
     * Creates a filesystem abstraction that can be used as an external dependency for your classes
     *
     * @return {@link Fs} instance
     */
    public static Fs fs() {
        return IoFs.instance();
    }

    public interface Entity {

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
         * Delets current {@link Fs.Entity}
         *
         * @throws Io.Err if something goes wrong
         */
        void delete() throws Io.Err;

        String name();

    }

    /**
     * Easy to use file abstraction suitable for mocking
     */
    public interface File extends Entity {

        /**
         * Creates a {@link BufferedReader} reading current file bytes
         *
         * @return {@link BufferedReader} for current file
         * @throws IOException if something goes wrong
         */
        BufferedReader reader() throws IOException;

        /**
         * Reads current file as an {@link BufferedReader} passed to the <code>handler</code>
         * provided. Stream is being automatically closed after <code>hanlder</code> returns. Result
         * of <code>handler</code> invocation is returned from this method as is.
         *
         * @param <T> type of a result to return
         * @param handler function receiving file's {@link BufferedReader} and producing output of
         * type <code>T</code>
         * @return result of a <code>handler</code> function invocation
         * @throws Io.Err if something goes wrong during I/O
         */
        <T> T reader(Function<BufferedReader, T> handler) throws Io.Err;

        /**
         * Reads current file as an {@link InputStream}
         *
         * @return {@link InputStream} of a current file
         * @throws Io.Err if something goes wrong
         */
        InputStream stream() throws Io.Err;

        /**
         * Reads current file as an {@link InputStream} passed to the <code>handler</code> provided.
         * Reader is being automatically closed after <code>hanlder</code> returns. Result of
         * <code>handler</code> invocation is returned from this method as is.
         *
         * @param <T> type of a result to return
         * @param handler function receiving file's {@link InputStream} and producing output of type
         * <code>T</code>
         * @return result of a <code>handler</code> function invocation
         * @throws Io.Err if something goes wrong during I/O
         */
        <T> T stream(Function<InputStream, T> handler) throws Io.Err;

        /**
         * Reads target file contents to a single String
         *
         * @return target file content as a single String
         * @throws Io.Err if something goes wrong
         */
        String string() throws Io.Err;

        /**
         * Writes bytes to a current file
         *
         * @param data bytes to write
         * @throws Io.Err if something goes wrong
         */
        void write(byte[] data) throws Io.Err;

        /**
         * Writes to current file using its {@link BufferedWriter} passed to the
         * <code>handler</code> provided. Writer is being automatically closed after
         * <code>hanlder</code> returns.
         *
         * @param handler function receiving file's {@link BufferedWriter}
         * @throws Io.Err if something goes wrong during I/O
         */
        void write(Consumer<BufferedWriter> handler) throws Io.Err;

        void copyTo(File target) throws Io.Err;
    }

    public interface Dir extends Entity {

        Stream<Entity> stream() throws Err;
    }

    /**
     * Filesystem abstraction mostly for dependency injection and easy mocking
     */
    public interface Fs {

        /**
         * Creates a {@link Io.File} instance on a given path
         *
         * @param first first part of a file path
         * @param more other parts of a file path
         * @return {@link Io.File} instance bound to a given path
         * @throws Io.Err if this <code>path</code> corresponds to a directory
         */
        File file(String first, String... more) throws Io.Err;

        /**
         * Creates a {@link Io.Dir} instance on a given path
         *
         * @param first first part of a dir path
         * @param more other parts of a dir path
         * @return {@link Io.Dir} object bound to a given path
         * @throws Io.Err if <code>path</code> corresponds to a file
         */
        Dir dir(String first, String... more) throws Io.Err;

        /**
         * Creates a directory in a system standard tmp location
         *
         * @return temporary {@link Io.Dir}
         * @throws Io.Err if something goes wrong
         */
        Dir tempDir() throws Io.Err;
    }

    /**
     * This exception is thrown when something goes fubar during I/O. See its reason for details.
     */
    public static class Err extends Exception {

        Err(String message) {
            super(message);
        }

        Err(Throwable cause) {
            super(cause);
        }

        static Io.Err from(Exception ex) {
            if (ex instanceof Io.Err) {
                return (Io.Err) ex;
            }
            return new Io.Err(ex);
        }
    }

    private static abstract class IoEntity implements Io.Entity {

        protected final Path path;

        IoEntity(String first, String... more) {
            this(Paths.get(first, more));
        }

        IoEntity(Path path) {
            this.path = path;
        }

        @Override
        public String path() {
            return path.toString();
        }

        @Override
        public String toString() {
            return this.getClass().getSimpleName() + " at " + path;
        }

        @Override
        public String name() {
            return path.getFileName().toString();
        }
    }

    private static class IoFile extends IoEntity implements Io.File {

        IoFile(String first, String... more) throws Err {
            super(first, more);
            throwIfDirectory();
        }

        private void throwIfDirectory() throws Err {
            if (Files.isDirectory(path)) {
                throw new Io.Err(path() + " is a directory");
            }
        }

        IoFile(Path path) throws Err {
            super(path);
            throwIfDirectory();
        }

        @Override
        public void write(byte[] data) throws Io.Err {
            Errors.rethrow(() -> Files.write(path, data), Err::from);
        }

        @Override
        public void write(Consumer<BufferedWriter> handler) throws Io.Err {
            Errors.rethrow(() -> {
                try (final BufferedWriter writer = Files.newBufferedWriter(path)) {
                    handler.accept(writer);
                }
            }, Err::from);
        }

        @Override
        public boolean exists() {
            return Files.exists(path);
        }

        @Override
        public String string() throws Io.Err {
            return Errors.rethrow(() -> new String(Files.readAllBytes(path)), Err::from);
        }

        @Override
        public BufferedReader reader() throws IOException {
            return Files.newBufferedReader(path);
        }

        @Override
        public <T> T reader(Function<BufferedReader, T> handler) throws Io.Err {
            return Errors.rethrow(() -> {
                try (final BufferedReader reader = reader()) {
                    return handler.apply(reader);
                }
            }, Err::from);
        }

        @Override
        public InputStream stream() throws Io.Err {
            return Errors.rethrow(() -> Files.newInputStream(path), Err::from);
        }

        @Override
        public <T> T stream(Function<InputStream, T> handler) throws Io.Err {
            return Errors.rethrow(() -> {
                try (final InputStream stream = stream()) {
                    return handler.apply(stream);
                }
            }, Err::from);
        }

        @Override
        public void delete() throws Err {
            Errors.rethrow(() -> Files.delete(path), Err::from);
        }

        @Override
        public void copyTo(File target) throws Err {
            Errors.rethrow(() -> Files.copy(
                    this.path,
                    ((IoEntity)target).path,
                    StandardCopyOption.REPLACE_EXISTING
            ), Err::from);
        }
    }

    private static class IoDir extends IoEntity implements Io.Dir {

        IoDir(String first, String... more) throws Err {
            super(first, more);
            throwIfFile();
        }

        IoDir(Path path) throws Err {
            super(path);
            throwIfFile();
        }

        private void throwIfFile() throws Err {
            if (!Files.isDirectory(path)) {
                throw new Io.Err(path() + " is a directory");
            }
        }

        @Override
        public boolean exists() {
            return Files.exists(path) && Files.isDirectory(path);
        }

        @Override
        public void delete() throws Io.Err {
            for (Io.Entity e : stream().collect(Collectors.toList())) {
                e.delete();
            }
        }

        @Override
        public Stream<Io.Entity> stream() throws Io.Err {
            Errors.Catcher<Err> error = Errors.catcher(Io.Err.class);
            Stream<Io.Entity> stream = Errors.rethrow(() -> Files
                    .list(path)
                    .map(f -> error.safeCall(() -> Files.isDirectory(f)
                                            ? new IoDir(f)
                                            : new IoFile(f))), Err::from)
                    .filter(Optional::isPresent)
                    .map(Optional::get);
            error.rethrow();
            return stream;
        }
    }

    private static class IoFs implements Io.Fs {

        private static final IoFs INSTANCE = new IoFs();

        public static IoFs instance() {
            return INSTANCE;
        }

        private IoFs() {
        }

        @Override
        public Io.File file(String first, String... more) throws Err {
            return new IoFile(first, more);
        }

        @Override
        public Io.Dir tempDir() throws Io.Err {
            return Errors.rethrow(() -> new IoDir(Files.createTempDirectory("")), Err::from);
        }

        @Override
        public Io.Dir dir(String first, String... more) throws Err {
            return new IoDir(first, more);
        }
    }

}
