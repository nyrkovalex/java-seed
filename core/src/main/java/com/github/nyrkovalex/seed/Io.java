package com.github.nyrkovalex.seed;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;
import java.util.function.Function;

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

	/**
	 * Easy to use file abstraction suitable for mocking
	 */
	public static interface File {

		/**
		 * Deletes target file or directory with its contents, just like <code>rm -rf</code> would
		 *
		 * @throws Io.Err if something goes wrong
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

	/**
	 * Filesystem abstraction mostly for dependency injection and easy mocking
	 */
	public static interface Fs {

		/**
		 * Creates a {@link Io.File} instance on a given path
		 *
		 * @param path path to a file
		 * @return {@link Io.File} instance bound to a given path
		 */
		File file(String path);
	}

	/**
	 * This exception is thrown when something goes fubar during I/O.
	 * See its reason for details.
	 */
	public static class Err extends Exception {

		private static final long serialVersionUID = 1L;

		Err(Throwable cause) {
			super(cause);
		}

		static void rethrow(VoidUnsafeCall call) throws Io.Err {
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

class IoFile implements Io.File {

	private final Path path;

	IoFile(String path) {
		this.path = Paths.get(path);
	}

	@Override
	public String path() {
		return path.toString();
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
	public void deleteWithContents() throws Io.Err {
		try {
			recurseDelete(path);
		} catch (RuntimeException ex) {
			throw new Io.Err(ex.getCause());
		}
	}

	private static void recurseDelete(Path f) throws RuntimeException {
		try {
			if (Files.isDirectory(f)) {
				Files.list(f).forEach(IoFile::recurseDelete);
			}
			Files.deleteIfExists(f);
		} catch (IOException ex) {
			// Propagate as runtime exception so we can use method reference above
			throw new RuntimeException(ex);
		}
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
	public Io.File file(String path) {
		return new IoFile(path);
	}
}
