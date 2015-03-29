package com.github.nyrkovalex.seed.plugins;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

class PlugLoader implements Plugins.Path {

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
		List<URL> plugins = readDirectory(Paths.get(path));
		return new URLClassLoader(plugins.toArray(new URL[plugins.size()]));
	}

	@Override
	public Object instanceOf(String className) throws Plugins.Err {
		try {
			Class<?> loaded = classLoader.loadClass(className);
			return loaded.newInstance();
		} catch (ReflectiveOperationException ex) {
			throw new Plugins.Err(ex);
		}
	}

	@Override
	public ClassLoader classLoader() {
		return classLoader;
	}

}
