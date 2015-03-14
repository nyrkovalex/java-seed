
package com.github.nyrkovalex.seed;

import com.github.nyrkovalex.seed.Seed;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;
import java.util.function.Function;

class Fs implements Seed.Fs {

    private static final Fs INSTANCE = new Fs();

    public static Fs instance() {
        return INSTANCE;
    }

    private Fs() { }

    @Override
    public Seed.File file(String path) {
        return new File(path);
    }
}


class File implements Seed.File {

    private final Path path;

    File(String path) {
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
        try(BufferedWriter writer = Files.newBufferedWriter(path)) {
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
        try(BufferedReader reader = reader()) {
            return handler.apply(reader);
        }
    }

    @Override
    public InputStream stream() throws IOException {
        return Files.newInputStream(path);
    }

    @Override
    public <T> T stream(Function<InputStream, T> handler) throws IOException {
        try(InputStream stream = stream()) {
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
            if (java.nio.file.Files.isDirectory(f)) {
                java.nio.file.Files.list(f).forEach(File::recurseDelete);
            }
            java.nio.file.Files.deleteIfExists(f);
        } catch (IOException ex) {
            // Propagate as runtime exception so we can use method reference above
            throw new RuntimeException(ex);
        }
    }
}
