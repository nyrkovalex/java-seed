package com.github.nyrkovalex.seed;

import com.github.nyrkovalex.seed.Seed;
import java.io.PrintWriter;

class SeedConsole implements Seed.Console {

    private static final SeedConsole INSTANCE = new SeedConsole();

    public static SeedConsole instance() {
        return INSTANCE;
    }

    private SeedConsole() {
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
