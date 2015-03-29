
package com.github.nyrkovalex.seed.sys;

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
