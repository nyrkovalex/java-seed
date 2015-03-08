package com.github.nyrkovalex.seed.core;

import java.io.PrintWriter;

class Console implements Seed.Console {

    private static final Console INSTANCE = new Console();

    public static Console instance() {
        return INSTANCE;
    }

    private Console() {
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
