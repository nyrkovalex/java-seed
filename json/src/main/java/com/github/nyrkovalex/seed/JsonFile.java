/*
 * The MIT License
 *
 * Copyright 2015 Alexander Nyrkov.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.github.nyrkovalex.seed;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Optional;

class JsonFile<T> implements Json.File<T> {

    private final Seed.File file;
    private final Json.Parser parser;
    private final Class<T> clazz;

    JsonFile(Seed.File file, Json.Parser parser, Class<T> clazz) {
        this.file = file;
        this.parser = parser;
        this.clazz = clazz;
    }

    @Override
    public T read() throws IOException {
        return file.reader(r -> parser.read(r).as(clazz));
    }

    @Override
    public Optional<T> readIfExists() throws IOException {
        if (file.exists()) {
            return Optional.of(read());
        }
        return Optional.empty();
    }

    @Override
    public void write(T data) throws IOException {
        Seed.Error<IOException> err = Seed.error(IOException.class);
        file.write((BufferedWriter w) -> {
            err.safeCall(() -> parser.write(data).to(w));
        });
        err.rethrow();
    }
}
