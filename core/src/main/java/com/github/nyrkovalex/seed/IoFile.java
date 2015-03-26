
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

import com.github.nyrkovalex.seed.Io.Err;

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

    /**
     * Deletes target file or directory with its contents, just like <code>rm -rf</code> would
     *
     * @throws IOException if something goes wrong
     */
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
