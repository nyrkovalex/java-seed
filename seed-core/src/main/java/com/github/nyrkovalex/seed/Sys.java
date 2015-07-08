package com.github.nyrkovalex.seed;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;

public final class Sys {

  private Sys() {
    // Module
  }

  public static Console console() {
    return SysConsole.instance();
  }

  public static Clock clock() {
    return () -> Instant.now();
  }

  public static Env env() {
    return SysEnv.instance();
  }

  /**
   * Simple console abstraction. Can be used for dependency injection and mocking
   */
  public interface Console {

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

  public interface Clock {

    Instant now();
  }

  public interface Env {

    String read(String varName);

    String cwd();

    String userHome();
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

class SysEnv implements Sys.Env {

  private static final SysEnv INSTANCE = new SysEnv();

  private SysEnv() {
    // Singleton
  }

  public static SysEnv instance() {
    return INSTANCE;
  }

  @Override
  public String read(String varName) {
    return System.getenv(varName);
  }

  @Override
  public String cwd() {
    try {
      return new File(".").getCanonicalPath();
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public String userHome() {
    return System.getProperty("user.home");
  }

}
