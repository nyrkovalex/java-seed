package com.github.nyrkovalex.seed;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.util.Optional;

/**
 * Json module encapsulates json parsing logic
 */
public final class Json {

    private Json() {
    }

    /**
     * Parser contains json parsing logic and server as a factory for {@link Json.File} type.
     */
    public interface Parser {

        /**
         * Read json data from a reader as an instance of a type specified by a {@link ReadCommand}.
         *
         * @param reader reader to extract json from
         * @return {@link ReadCommand} specifying additional reading options
         *
         * @see ReadCommand
         * @see Reader
         */
        ReadCommand read(Reader reader);

        /**
         * Dumps an object provided to a json format sent to a {@link Writer} specified by a
         * {@link WriteCommand}.
         *
         * @param data object to dump into json
         * @return {@link WriteCommand} specifying additional writing options
         */
        WriteCommand write(Object data);

        /**
         * Creates a {@link Json.File} instance using current {@link Json.Parser}.
         *
         * @param <T> type to parse json to
         * @param file {@link Io.File} used as a json source
         * @param clazz class to parse json to
         * @return {@link Json.File} bound to current parser and provided params
         */
        <T> Json.File<T> file(Io.File file, Class<? extends T> clazz);

        /**
         * Specifies additional json reading params
         */
        interface ReadCommand {

            /**
             * Parses json to an instance of a class provided
             *
             * @param <T> type of an instance being created
             * @param clazz to parse json to
             * @return json data parsed to the object of a provided type
             */
            <T> T as(Class<T> clazz);
        }

        /**
         * Specifies additional json writing params
         */
        interface WriteCommand {

            /**
             * Writes json data to a writer provided
             *
             * @param writer writer to write json to
             * @throws Err if object cannot be dumped to json
             */
            void to(Writer writer) throws Err;
        }
    }

    /**
     * Returns an instance of a {@link Json.Parser}.
     *
     * @return json parser ready to rock
     */
    public static Parser parser() {
        return JsonParser.instance();
    }

    /**
     * Json.File is an abstraction sitting on shoulders of {@link Json.Parser} and {@link Io.File}.
     * Supports basic reading and writing.
     *
     * @param <T> type of an object being parsed
     */
    public interface File<T> {

        /**
         * Reads json from target file to an instance of type <code>T</code>
         *
         * @return COntent of a file parsed to an instance of <code>T</code>
         * @throws Io.Err if file content cannot be parsed to type <code>T</code>
         */
        T read() throws Io.Err;

        /**
         * <p>
         * Reads file if exists and returns its content as an {@link Optional} of type
         * <code>T</code>.
         * </p>
         * <p>
         * If no file exists on a give path it returns {@link Optional#empty() }.
         * </p>
         *
         * @return {@link Optional} of type <code>T</code> with file content parsed to it or
         * {@link Optional#empty() }
         * @throws Io.Err
         */
        Optional<T> readIfExists() throws Io.Err;

        /**
         * Writes data provided to an underlying file as a json string
         *
         * @param data data to write to file
         * @throws Json.Err if <code>data</code> cannot be dumped to json or file cannot be written
         * to
         * @throws Io.Err if something bad happens while writing to file
         */
        void write(T data) throws Json.Err, Io.Err;
    }

    public static class Err extends Exception {

        Err(Throwable cause) {
            super(cause);
        }
    }

    private static class JsonFile<T> implements Json.File<T> {

        private final Io.File file;
        private final Json.Parser parser;
        private final Class<? extends T> clazz;

        JsonFile(Io.File file, Json.Parser parser, Class<? extends T> clazz) {
            this.file = file;
            this.parser = parser;
            this.clazz = clazz;
        }

        @Override
        public T read() throws Io.Err {
            return file.reader(r -> parser.read(r).as(clazz));
        }

        @Override
        public Optional<T> readIfExists() throws Io.Err {
            if (file.exists()) {
                return Optional.of(read());
            }
            return Optional.empty();
        }

        @Override
        public void write(T data) throws Json.Err, Io.Err {
            Errors.Catcher<Json.Err> err = Errors.catcher(Json.Err.class);
            file.write((BufferedWriter w) -> {
                err.safeCall(() -> parser.write(data).to(w));
            });
            err.rethrow();
        }
    }

    private static class JsonParser implements Json.Parser {

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
        public WriteCommand write(Object data) {
            return new WriteCommand(data);
        }

        private JsonParser() {
        }

        @Override
        public <T> Json.File<T> file(Io.File file, Class<? extends T> clazz) {
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
            public void to(Writer writer) throws Json.Err {
                try {
                    writer.write(GSON.toJson(data));
                } catch (IOException ex) {
                    throw new Json.Err(ex);
                }
            }
        }
    }
}
