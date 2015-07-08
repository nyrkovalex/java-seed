package com.github.nyrkovalex.seed.logging;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Contains helping functions for logging jumpstart using {@link Logger}
 */
public final class Logging {

	private Logging() {
	}

	/**
	 * Creates a {@link Logger} for a given {@link Class} using its name as a {@link Logger} name
	 *
	 * @param clazz class to create a {@link Logger} for
	 * @return {@link Logger} for a given class
	 * @see Logger
	 */
	public static Logger logger(Class<?> clazz) {
		return Logger.getLogger(clazz.getName());
	}

	/**
	 * Initializes root {@link Logger} and its {@link Handler}s depending on
	 * argument provided and
	 * {@link com.github.nyrkovalex.seed.core.Seed.Logging.DetailedFormatter}
	 *
	 * @param debugEnabled
	 *          whether log debug statements (lower than {@link Level#INFO} or not
	 * @param clazz
	 *          apply such settings to target class' package and its children
	 */
	public static void init(boolean debugEnabled, Class<?> clazz) {
		init(debugEnabled, clazz.getPackage().getName());
	}

	/**
	 * Initializes root {@link Logger} and its {@link Handler}s depending on
	 * argument provided and
	 * {@link com.github.nyrkovalex.seed.core.Seed.Logging.DetailedFormatter}
	 *
	 * @param debugEnabled
	 *          whether log debug statements (lower than {@link Level#INFO} or not
	 * @param rootPackage
	 *          apply such settings to target package and its children
	 */
	public static void init(boolean debugEnabled, String rootPackage) {
		init(debugEnabled, rootPackage, debugEnabled ? new DetailedFormatter() : new StdOutFormatter());
	}

	/**
	 * Initializes root {@link Logger} and its {@link Handler}s depending on
	 * argument provided
	 *
	 * @param debugEnabled
	 *          whether log debug statements (lower than {@link Level#INFO} or not
	 * @param clazz
	 *          apply such settings to target class' package and its children
	 * @param formatter
	 *          formatter to be used for all loggers
	 */
	public static void init(boolean debugEnabled, Class<?> clazz, Formatter formatter) {
		init(debugEnabled, clazz.getPackage().getName(), formatter);
	}

	/**
	 * Initializes root {@link Logger} and its {@link Handler}s depending on
	 * argument provided
	 *
	 * @param debugEnabled
	 *          whether log debug statements (lower than {@link Level#INFO} or not
	 * @param rootPackage
	 *          apply such settings to target package and its children
	 * @param formatter
	 *          formatter to be used for all loggers
	 */
	public static void init(boolean debugEnabled, String rootPackage, Formatter formatter) {
		Level targetLevel = debugEnabled ? Level.FINEST : Level.INFO;
		Logger rootLogger = Logger.getLogger("");
		for (Handler h : rootLogger.getHandlers()) {
			h.setLevel(targetLevel);
			h.setFormatter(formatter);
		}
		Logger logger = Logger.getLogger(rootPackage);
		logger.setLevel(targetLevel);
	}

	/**
	 * Formats detailed log record as follows:
	 * <code>date [ LEVEL ] - class.Name: time</code>
	 */
	public static class DetailedFormatter extends Formatter {

		@Override
		public String format(LogRecord record) {
			return String.format("%s [ %s ] - %s: %s\n",
					new SimpleDateFormat("YYYY-MM-dd HH:mm:ss.SSS").format(new Date(record.getMillis())), record.getLevel(),
					record.getSourceClassName(), record.getMessage());
		}
	}

	/**
	 * Simply writes log message with no details around
	 */
	public static class StdOutFormatter extends java.util.logging.Formatter {

		@Override
		public String format(LogRecord record) {
			return record.getMessage() + "\n";
		}
	}
}
