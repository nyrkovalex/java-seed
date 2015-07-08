package com.gtihub.nyrkovalex.seed.nio;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.Iterator;

public class Fs {
	private static final Fs INSTANCE = new Fs();

	private Fs() {

	}

	public Path path(String first, String... more) {
		return Paths.get(first, more);
	}

	public Path tempDir(String prefix, FileAttribute<?>... attrs) throws IOException {
		return Files.createTempDirectory(prefix, attrs);
	}

	public Path tempFile(String prefix, String suffix, FileAttribute<?> attrs) throws IOException {
		return Files.createTempFile(prefix, suffix, attrs);
	}

	public Path copy(Path source, Path target, CopyOption... options) throws IOException {
		return Files.copy(source, target, options);
	}

	public void delete(Path path) throws IOException {
		Files.delete(path);
	}

	public void deleteWithContents(Path path) throws IOException {
		Iterator<Path> iter = Files.list(path).iterator();
		while (iter.hasNext()) {
			Path next = iter.next();
			if (isDirectory(next)) {
				deleteWithContents(next);
			} else {
				delete(next);
			}
		}
		delete(path);
	}

	public boolean isDirectory(Path path, LinkOption... options) {
		return Files.isDirectory(path, options);
	}

	public Path write(Path path, Iterable<? extends CharSequence> lines, OpenOption... options) throws IOException {
		return Files.write(path, lines, options);
	}

	public BufferedWriter newBufferedWriter(Path path, OpenOption... options) throws IOException {
		return Files.newBufferedWriter(path, options);
	}

	public BufferedReader newBufferedReader(Path path) throws IOException {
		return Files.newBufferedReader(path);
	}

	public boolean deleteIfExists(Path path) throws IOException {
		return Files.deleteIfExists(path);
	}

	public boolean exists(Path path, LinkOption... options) {
		return Files.exists(path, options);
	}

	public static Fs instance() {
		return INSTANCE;
	}
}
