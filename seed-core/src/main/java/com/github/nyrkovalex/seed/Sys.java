package com.github.nyrkovalex.seed;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
import java.util.Optional;

public final class Sys {

	private Sys() {
		// Module
	}

	/**
	 * Simplified console abstraction. Can be used for dependency injection and mocking
	 */
	public static class Console {

		private static final Console INSTANCE = new Console();
		private final java.io.Console console;

		public static Console instance() {
			return INSTANCE;
		}

		private Console() {
			console = System.console();
		}

		/**
		 * Reads user input from a console displaying a given prompt message
		 *
		 * @param prompt prompt message to display
		 * @return user input as a {@link String}
		 */
		public String readLine(String prompt) {
			return console.readLine(prompt);
		}

		/**
		 * Reads secure user input hiding actual typed characters from a console displaying a given
		 * prompt message
		 *
		 * @param prompt prompt message to display
		 * @return user input as a {@link String}
		 */
		public String readSecure(String prompt) {
			return String.copyValueOf(console.readPassword(prompt));
		}

		/**
		 * Prints formatted output to a console
		 *
		 * @param message message template
		 * @param args    arguments
		 */
		public void printf(String message, Object... args) {
			try (PrintWriter writer = console.writer()) {
				writer.printf(message, args);
			}
		}
	}

	public static class Environment {

		private static final Environment INSTANCE = new Environment();

		private Environment() {
			// Singleton
		}

		public static Environment instance() {
			return INSTANCE;
		}

		public Optional<String> readVar(String varName) {
			return Optional.ofNullable(System.getenv(varName));
		}

		public String cwd() {
			try {
				return new File(".").getCanonicalPath();
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}
		}

		public String userHome() {
			return System.getProperty("user.home");
		}

	}

	public static Console console() {
		return Console.instance();
	}

	public static Clock clock() {
		return Instant::now;
	}

	public static Environment environment() {
		return Environment.instance();
	}

	public interface Clock {
		Instant now();
	}
}
