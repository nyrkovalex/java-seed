
package com.github.nyrkovalex.seed;

import java.io.IOException;
import java.io.Reader;
import java.util.Optional;

/**
 * Json module encapsulates json parsing logic
 */
public final class Json {
    private Json() {
    }

    public static interface Parser {

        ReadCommand read(Reader reader);

        WriteCommand write(Object data) throws IOException;

        <T> Json.File<T> file(Seed.File file, Class<T> clazz);

        public static interface ReadCommand {

            <T> T as(Class<T> clazz);
        }

        public static interface WriteCommand {

            void to(Seed.File file) throws IOException;
        }
    }

    public static Parser parser() {
        return JsonParser.instance();
    }

    public static interface File<T> {

        Optional<T> read() throws IOException;

        void write(T data) throws IOException;
    }
}
