package com.github.nyrkovalex.seed.sys;

import java.io.PrintWriter;

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
