package com.github.nyrkovalex.seed.core;

import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

@SuppressWarnings("UnusedDeclaration")
public final class Seed {
    private Seed() {}

    public static class Logging {
        private Logging() {}

        public static void init(boolean debugEnabled, Class<?> clazz) {
            init(debugEnabled, clazz.getPackage().getName());
        }

        public static void init(boolean debugEnabled, String rootPackage) {
            init(debugEnabled, rootPackage, new DetailedFormatter());
        }

        public static void init(boolean debugEnabled, Class<?> clazz, Formatter formatter) {
            init(debugEnabled, clazz.getPackage().getName(), formatter);
        }

        public static void init(boolean debugEnabled, String rootPackage, Formatter formatter) {
            Level targetLevel = debugEnabled ? Level.FINEST : Level.INFO;
            Logger rootLogger = Logger.getLogger("");
            for (Handler h : rootLogger.getHandlers()) {
                h.setLevel(targetLevel);
                h.setFormatter(formatter);
            }
            Logger gitdepsLogger = Logger.getLogger(rootPackage);
            gitdepsLogger.setLevel(targetLevel);
        }

        public static class DetailedFormatter extends Formatter {
            @Override
            public String format(LogRecord record) {
                return String.format(
                        "%s [ %s ] - %s: %s\n",
                        new SimpleDateFormat("YYYY-MM-dd HH:mm:ss:SSS").format(
                                new Date(record.getMillis())),
                        record.getLevel(),
                        record.getSourceClassName(),
                        record.getMessage()
                );
            }
        }

        public static class StdOutFormatter extends java.util.logging.Formatter {
            @Override
            public String format(LogRecord record) {
                return record.getMessage() + "\n";
            }
        }
    }

    public static class Files {
        Files() {}

        public static void deleteWithContents(Path f) throws IOException {
            try {
                recurseDelete(f);
            } catch (RuntimeException ex) {
                throw new IOException(ex.getCause());
            }
        }

        private static void recurseDelete(Path f) {
            try {
                if (java.nio.file.Files.isDirectory(f)) {
                    java.nio.file.Files.list(f).forEach(Files::recurseDelete);
                }
                java.nio.file.Files.deleteIfExists(f);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
