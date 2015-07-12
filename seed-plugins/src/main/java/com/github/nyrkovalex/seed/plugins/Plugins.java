package com.github.nyrkovalex.seed.plugins;

import java.nio.file.Path;

public final class Plugins {

	private static final Loader LOADER = new Loader() {
		@Override
		public Repo repo(String path) {
			return new Repository(path);
		}

		@Override
		public Repo repo(Path path) {
			return new Repository(path);
		}
	};

	private Plugins() {
		// Module
	}

	/**
	 * Creates a new {@link Loader} instance
	 *
	 * @return {@link Loader} object serving as a {@link Repository} factory
	 */
	public static Loader loader() {
		return LOADER;
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

		/**
		 * Creates {@link Plugins.Repo} on a given <code>path</code>
		 *
		 * @param path path of a directory or single jar file to scan for java classes
		 * @return {@link Repo} instance bound to a given <code>path</code>
		 */
		Repo repo(Path path);
	}

}
