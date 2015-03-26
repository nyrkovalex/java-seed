package com.github.nyrkovalex.seed;

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
    public static interface Parser {

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
         * @param file {@link Seed.File} used as a json source
         * @param clazz class to parse json to
         * @return {@link Json.File} bound to current parser and provided params
         */
        <T> Json.File<T> file(Io.File file, Class<T> clazz);

        /**
         * Specifies additional json reading params
         */
        public static interface ReadCommand {

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
        public static interface WriteCommand {

            /**
             * Writes json data to a writer provided
             *
             * @param writer writer to write json to
             * @throws java.io.Io.Err if object cannot be dumped to json
             */
            void to(Writer writer) throws Io.Err;
        }
    }

    /**
     * Returns an instance of a {@link Json.Parser}.
     * @return json parser ready to rock
     */
    public static Parser parser() {
        return JsonParser.instance();
    }

    /**
     * Json.File is an abstraction sitting on shoulders of {@link Json.Parser} and {@link Seed.File}.
     * Supports basic reading and writing.
     *
     * @param <T> type of an object being parsed
     */
    public static interface File<T> {

        /**
         * Reads json from target file to an instance of type <code>T</code>
         *
         * @return COntent of a file parsed to an instance of <code>T</code>
         * @throws Io.Err if file content cannot be parsed to type <code>T</code>
         */
        T read() throws Io.Err;

        /**
         * <p>
         * Reads file if exists and returns its content as an {@link Optional}
         * of type <code>T</code>.
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
         * @throws Io.Err if <code>data</code> cannot be dumped to json
         * or file cannot be written to
         */
        void write(T data) throws Io.Err;
    }
}
