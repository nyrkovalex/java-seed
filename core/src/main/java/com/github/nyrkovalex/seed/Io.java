package com.github.nyrkovalex.seed;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import com.github.nyrkovalex.seed.Io.Entity;
import com.github.nyrkovalex.seed.Io.Err;
import com.github.nyrkovalex.seed.Io.File;
import java.util.Optional;
import java.util.stream.Collectors;

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

	public static interface Entity {

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

	}

	/**
	 * Easy to use file abstraction suitable for mocking
	 */
	public static interface File extends Entity {

		/**
		 * Creates a {@link BufferedReader} reading current file bytes
		 *
		 * @return {@link BufferedReader} for current file
		 * @throws IOException if something goes wrong
		 */
		BufferedReader reader() throws IOException;


		/**
		 * Reads current file as an {@link BufferedReader} passed to the <code>handler</code>
		 * provided.
		 * Stream is being automatically closed after <code>hanlder</code> returns.
		 * Result of <code>handler</code> invocation is returned from this method as is.
		 *
		 * @param <T> type of a result to return
		 * @param handler function receiving file's {@link BufferedReader} and producing output
		 * of type <code>T</code>
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
		 * @param handler function receiving file's {@link InputStream} and producing output
		 * of type <code>T</code>
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
		 * <code>handler</code> provided.
		 * Writer is being automatically closed after <code>hanlder</code> returns.
		 *
		 * @param handler function receiving file's {@link BufferedWriter}
		 * @throws Io.Err if something goes wrong during I/O
		 */
		void write(Consumer<BufferedWriter> handler) throws Io.Err;
	}

	public static interface Dir extends Entity {
        Stream<Entity> stream() throws Err;
	}

	/**
	 * Filesystem abstraction mostly for dependency injection and easy mocking
	 */
	public static interface Fs {

		/**
		 * Creates a {@link Io.File} instance on a given path
		 *
		 * @param path path to a file
		 * @return {@link Io.File} instance bound to a given path
		 * @throws Io.Err if this <code>path</code> corresponds to a directory
		 */
		File file(String path) throws Io.Err;

		/**
		 * Creates a {@link Io.Dir} instance on a given path
		 *
		 * @param path directory path
		 * @return {@link Io.Dir} object bound to a given path
		 * @throws Io.Err if <code>path</code> corresponds to a file
		 */
		Dir dir(String path) throws Io.Err;

		/**
		 * Creates a directory in a system standard tmp location
		 *
		 * @return temporary {@link Io.Dir}
		 * @throws Io.Err if something goes wrong
		 */
		Dir tempDir() throws Io.Err;
	}

	/**
	 * This exception is thrown when something goes fubar during I/O.
	 * See its reason for details.
	 */
	public static class Err extends Exception {

		Err(String message) {
			super(message);
		}

		Err(Throwable cause) {
			super(cause);
		}

		static void rethrow(Seed.VoidUnsafeCall call) throws Io.Err {
			try {
				call.call();
			} catch (Exception ex) {
				throw from(ex);
			}
		}

		static Io.Err from(Exception ex) {
			if (ex instanceof Io.Err) {
				return (Io.Err) ex;
			}
			return new Io.Err(ex);
		}

		static <T> T rethrow(Seed.UnsafeCall<T> call) throws Io.Err {
			try {
				return call.call();
			} catch (Exception ex) {
				throw from(ex);
			}
		}
	}

}


abstract class IoEntity implements Io.Entity {
    protected final Path path;

    IoEntity(String path) {
        this(Paths.get(path));
    }

    IoEntity(Path path) {
        this.path = path;
    }

    @Override
    public String path() {
        return path.toString();
    }
}


class IoFile extends IoEntity implements Io.File {

    IoFile(String strpath) throws Err {
        super(strpath);
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
		Io.Err.rethrow(() -> Files.write(path, data));
	}

	@Override
	public void write(Consumer<BufferedWriter> handler) throws Io.Err {
		Io.Err.rethrow(() -> {
			try (final BufferedWriter writer = Files.newBufferedWriter(path)) {
				handler.accept(writer);
			}
		});
	}

	@Override
	public boolean exists() {
		return Files.exists(path);
	}

	@Override
	public String string() throws Io.Err {
		return Io.Err.rethrow(() -> new String(Files.readAllBytes(path)));
	}

	@Override
	public BufferedReader reader() throws IOException {
		return Files.newBufferedReader(path);
	}

	@Override
	public <T> T reader(Function<BufferedReader, T> handler) throws Io.Err {
		return Io.Err.rethrow(() -> {
			try (final BufferedReader reader = reader()) {
				return handler.apply(reader);
			}
		});
	}

	@Override
	public InputStream stream() throws Io.Err {
		return Io.Err.rethrow(() -> Files.newInputStream(path));
	}

	@Override
	public <T> T stream(Function<InputStream, T> handler) throws Io.Err {
		return Io.Err.rethrow(() -> {
			try (final InputStream stream = stream()) {
				return handler.apply(stream);
			}
		});
	}

    @Override
    public void delete() throws Err {
	    Io.Err.rethrow(() -> Files.delete(path));
    }
}


class IoDir extends IoEntity implements Io.Dir {

    IoDir(String strpath) throws Err {
       super(strpath);
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
		Seed.Error<Err> error = Seed.error(Io.Err.class);
		Stream<Io.Entity> stream = Io.Err.rethrow(() -> Files
				.list(path)
				.map(f -> error.safeCall(() -> Files.isDirectory(f)
						? new IoDir(f)
						: new IoFile(f))))
				.filter(Optional::isPresent)
				.map(Optional::get);
		error.rethrow();
		return stream;
    }
}


class IoFs implements Io.Fs {

	private static final IoFs INSTANCE = new IoFs();

	public static IoFs instance() {
		return INSTANCE;
	}

	private IoFs() {
	}

	@Override
	public Io.File file(String path) throws Err {
		return new IoFile(path);
	}

	@Override
	public Io.Dir tempDir() throws Io.Err {
		return Io.Err.rethrow(() -> new IoDir(Files.createTempDirectory("")));
	}

	@Override
	public Io.Dir dir(String path) throws Err {
		return new IoDir(path);
	}
}
