
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

class SeedFile implements Seed.File {
    private final Path path;

    SeedFile(String path) {
        this.path = Paths.get(path);
    }

    @Override
    public String path() {
        return path.toString();
    }

    @Override
    public void write(byte[] data) throws IOException {
        Files.write(path, data);
    }

    @Override
    public void write(Consumer<BufferedWriter> handler) throws IOException {
        try (final BufferedWriter writer = Files.newBufferedWriter(path)) {
            handler.accept(writer);
        }
    }

    @Override
    public boolean exists() {
        return Files.exists(path);
    }

    @Override
    public String string() throws IOException {
        return new String(Files.readAllBytes(path));
    }

    @Override
    public BufferedReader reader() throws IOException {
        return Files.newBufferedReader(path);
    }

    @Override
    public <T> T reader(Function<BufferedReader, T> handler) throws IOException {
        try (final BufferedReader reader = reader()) {
            return handler.apply(reader);
        }
    }

    @Override
    public InputStream stream() throws IOException {
        return Files.newInputStream(path);
    }

    @Override
    public <T> T stream(Function<InputStream, T> handler) throws IOException {
        try (final InputStream stream = stream()) {
            return handler.apply(stream);
        }
    }

    /**
     * Deletes target file or directory with its contents, just like <code>rm -rf</code> would
     *
     * @throws IOException if something goes wrong
     */
    @Override
    public void deleteWithContents() throws IOException {
        try {
            recurseDelete(path);
        } catch (RuntimeException ex) {
            throw new IOException(ex.getCause());
        }
    }

    private static void recurseDelete(Path f) throws RuntimeException {
        try {
            if (Files.isDirectory(f)) {
                Files.list(f).forEach(SeedFile::recurseDelete);
            }
            Files.deleteIfExists(f);
        } catch (IOException ex) {
            // Propagate as runtime exception so we can use method reference above
            throw new RuntimeException(ex);
        }
    }

}
