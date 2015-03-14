package com.github.nyrkovalex.seed;

import com.github.nyrkovalex.seed.Seed;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

class PluginLoader implements Seed.PluginLoader {
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

    @Override
    public ClassLoader forPath(String path) {
        List<URL> plugins = readDirectory(Paths.get(path));
        return new URLClassLoader(plugins.toArray(new URL[plugins.size()]));
    }
}
