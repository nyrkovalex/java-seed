
package com.github.nyrkovalex.seed;

import java.io.PrintWriter;
import java.time.Instant;

public final class Sys {
	private Sys() {
		// Module
	}

	public static Console console() {
		return new SysConsole();
	}

	public static Clock clock() {
		return () -> Instant.now();
	}


    /**
     * Simple console abstraction. Can be used for dependency injection and mocking
     */
    public static interface Console {

        /**
         * Prints formatted output to a console
         *
         * @param message message template
         * @param args arguments
         */
        void printf(String message, Object... args);

        /**
         * Reads user input from a console displaying a given prompt message
         *
         * @param prompt prompt message to display
         * @return user input as a {@link String}
         */
        String read(String prompt);

        /**
         * Reads secure user input hiding actual typed characters from a console displaying a given
         * prompt message
         *
         * @param prompt prompt message to display
         * @return user input as a {@link String}
         */
        String readSecure(String prompt);
    }

	public static interface Clock {
		public Instant now();
	}
}

class SysConsole implements Sys.Console {

    private static final SysConsole INSTANCE = new SysConsole();

    public static SysConsole instance() {
        return INSTANCE;
    }

    SysConsole() {
    }

    @Override
    public String read(String prompt) {
        return System.console().readLine(prompt);
    }

    @Override
    public String readSecure(String prompt) {
        return String.copyValueOf(System.console().readPassword(prompt));
    }

    @Override
    public void printf(String message, Object... args) {
        try (PrintWriter writer = System.console().writer()) {
            writer.printf(message, args);
        }
    }
}