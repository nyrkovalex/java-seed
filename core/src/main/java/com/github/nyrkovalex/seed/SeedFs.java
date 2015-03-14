
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

class SeedFs implements Seed.Fs {

    private static final SeedFs INSTANCE = new SeedFs();

    public static SeedFs instance() {
        return INSTANCE;
    }

    private SeedFs() { }

    @Override
    public Seed.File file(String path) {
        return new SeedFile(path);
    }
}


