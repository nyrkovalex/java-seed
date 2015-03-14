package com.github.nyrkovalex.seed.ssh;

import com.github.nyrkovalex.seed.Seed;
import com.jcraft.jsch.UserInfo;

class ConsoleUserInfo implements UserInfo {

    private final Seed.Console console;
    private String passphrase;
    private String password;

    public ConsoleUserInfo(Seed.Console console) {
        this.console = console;
    }

    @Override
    public String getPassphrase() {
        return passphrase;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean promptPassword(String message) {
        this.password = console.readSecure(beautify(message));
        return true;
    }

    @Override
    public boolean promptPassphrase(String message) {
        this.passphrase = console.readSecure(beautify(message));
        return true;
    }

    @Override
    public boolean promptYesNo(String message) {
        return console.read(beautify(message)).equalsIgnoreCase("y");
    }

    private static String beautify(String message) {
        return message + ": ";
    }

    @Override
    public void showMessage(String message) {
        console.printf(message);
    }
}
