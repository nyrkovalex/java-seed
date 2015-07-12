package com.github.nyrkovalex.seed.plugins;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public final class Plugins {

	private Plugins() {
		// Module
	}

	/**
	 * Creates a new {@link Loader} instance
	 *
	 * @return
	 */
	public static Loader loader() {
		return PlugLoader::new;
	}

	/**
	 * Plugin repository attached to a dir containing <code>jar</code> files or a single
	 * <code>jar</code>
	 */
	public interface Repo {

		/**
		 * Creates a {@link ClassLoader} for a given path. Such {@link ClassLoader} will load classes by
		 * recursing into the path provided and scanning all <code>*.jar</code> files found.
		 *
		 * @return {@link ClassLoader} capable of loading classes from a <code>path</code> provided
		 * @see ClassLoader
		 */
		ClassLoader classLoader();

		/**
		 * Instantiates an object of a class specified by <code>className</code> using its default
		 * constructor.
		 *
		 * @param className name of a class to load, e.g. <code>my.package.MyClass</code>
		 * @return instance of a class requested
		 * @throws ReflectiveOperationException if no default constructor can be found or it's
		 *                                      not accessible or hell knows why
		 */
		Object instanceOf(String className) throws ReflectiveOperationException;

		<T> T instanceOf(String className, Class<T> clazz) throws ReflectiveOperationException;
	}

	/**
	 * Loader creates {@link Plugins.Repo} on a given <code>path</code>
	 */
	public interface Loader {

		/**
		 * Creates {@link Plugins.Repo} on a given <code>path</code>
		 *
		 * @param path path of a directory or single jar file to scan for java classes
		 * @return {@link Repo} instance bound to a given <code>path</code>
		 */
		Repo repo(String path);
	}

	private static class PlugLoader implements Plugins.Repo {

		private final ClassLoader classLoader;

		public PlugLoader(String path) {
			this.classLoader = createClassLoader(path);
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

		private ClassLoader createClassLoader(String path) {
			final List<URL> plugins;
			Path target = Paths.get(path);
			if (Files.isDirectory(target)) {
				plugins = readDirectory(Paths.get(path));
			} else {
				plugins = Collections.singletonList(pathToUrl(target));
			}
			return new URLClassLoader(plugins.toArray(new URL[plugins.size()]));
		}

		@Override
		public Object instanceOf(String className) throws ReflectiveOperationException {
			Class<?> loaded = classLoader.loadClass(className);
			return loaded.newInstance();
		}

		@Override
		public <T> T instanceOf(String className, Class<T> clazz) throws ReflectiveOperationException {
			return clazz.cast(instanceOf(className));
		}

		@Override
		public ClassLoader classLoader() {
			return classLoader;
		}

	}
}
