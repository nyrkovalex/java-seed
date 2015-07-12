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

class Repository implements Plugins.Repo {

	private final ClassLoader classLoader;

	public Repository(String path) {
		this.classLoader = createClassLoader(path);
	}

	public Repository(Path path) {
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
		unsafeList(directory).forEach(p -> {
			if (!java.nio.file.Files.isDirectory(p)) {
				urls.add(pathToUrl(p));
				return;
			}
			urls.addAll(readDirectory(p));

		});
		return urls;
	}

	private static Stream<Path> unsafeList(Path directory) throws IllegalStateException {
		try {
			return java.nio.file.Files.list(directory);
		} catch (IOException ex) {
			throw new IllegalStateException("Failed to read directory", ex);
		}
	}

	private ClassLoader createClassLoader(String path) {
		Path target = Paths.get(path);
		return createClassLoader(target);
	}

	private ClassLoader createClassLoader(Path target) {
		final List<URL> plugins;
		if (Files.isDirectory(target)) {
			plugins = readDirectory(target);
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
