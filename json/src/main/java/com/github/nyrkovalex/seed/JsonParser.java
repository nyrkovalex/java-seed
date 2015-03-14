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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

class JsonParser implements Json.Parser {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private static final JsonParser INSTANCE = new JsonParser();

    public static JsonParser instance() {
        return INSTANCE;
    }

    @Override
    public ReadCommand read(Reader reader) {
        return new ReadCommand(reader);
    }

    @Override
    public WriteCommand write(Object data) throws IOException {
        return new WriteCommand(data);
    }

    private JsonParser() {
    }

    @Override
    public <T> Json.File<T> file(Seed.File file, Class<T> clazz) {
        return new JsonFile<>(file, this, clazz);
    }

    public static class ReadCommand implements Json.Parser.ReadCommand {

        private final Reader reader;

        private ReadCommand(InputStream in) {
            this(new InputStreamReader(in));
        }

        private ReadCommand(Reader reader) {
            this.reader = reader;
        }

        @Override
        public <T> T as(Class<T> clazz) {
            return GSON.fromJson(reader, clazz);
        }
    }

    public static class WriteCommand implements Json.Parser.WriteCommand {

        private final Object data;

        private WriteCommand(Object data) {
            this.data = data;
        }

        @Override
        public void to(Seed.File file) throws IOException {
            file.write(GSON.toJson(data).getBytes());
        }
    }
}
