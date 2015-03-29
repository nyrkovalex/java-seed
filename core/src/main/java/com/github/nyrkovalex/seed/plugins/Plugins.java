
package com.github.nyrkovalex.seed.plugins;

public final class Plugins {
	private Plugins() {
		// Module
	}

	public static Plug plug() {
		return (String path) -> new PlugLoader(path);
	}

	public static interface Path {

        /**
         * Creates a {@link ClassLoader} for a given path. Such {@link ClassLoader} will load
         * classes by recursing into the path provided and scanning all <code>*.jar</code> files
         * found.
         *
         * @return {@link Seed.Provider} of a {@link ClassLoader} capable of loading classes from a
         * <code>path</code> provided
         *
         * @see ClassLoader
         */
        ClassLoader classLoader();

		Object instanceOf(String className) throws Err;
	}

	public static interface Plug {
		Path path(String path);
	}

	public static class Err extends Exception {
		Err(Throwable cause) {
			super(cause);
		}
	}
}
