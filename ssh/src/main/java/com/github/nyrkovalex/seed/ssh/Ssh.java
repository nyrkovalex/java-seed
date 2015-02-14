package com.github.nyrkovalex.seed.ssh;

public final class Ssh {

    private Ssh() {
    }

    public static ScpCommand scp(String path) {
        return new ScpCommand(path);
    }

}
