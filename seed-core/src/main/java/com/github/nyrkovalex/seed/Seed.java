package com.github.nyrkovalex.seed;

import java.util.Arrays;

public final class Seed {

	private Seed() {
		// Module
	}

	/**
	 * Helper functions for {@link String} class
	 */
	public static final class Strings {

		private Strings() {
		}

		/**
		 * Joins items with provided separator.
		 *
		 * @param separator separator symbol
		 * @param items     items to join
		 * @return string representation of items joined with separator
		 */
		public static String join(String separator, Iterable<?> items) {
			StringBuilder builder = new StringBuilder();
			boolean appendSeparator = false;
			for (Object item : items) {
				if (appendSeparator) {
					builder.append(separator);
				}
				builder.append(item);
				appendSeparator = true;
			}
			return builder.toString();
		}

		/**
		 * Joins items with provided separator.
		 *
		 * @param separator separator symbol
		 * @param items     items to join
		 * @return string representation of items joined with separator
		 */
		public static String join(String separator, Object... items) {
			return join(separator, Arrays.asList(items));
		}

	}

}
