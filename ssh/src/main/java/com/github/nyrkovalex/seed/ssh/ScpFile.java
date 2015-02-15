package com.github.nyrkovalex.seed.ssh;

import java.io.File;

class ScpFile {

    private final String from;
    private final String to;

    ScpFile(String from, String to) {
        this.from = from;
        this.to = to;
    }

    public String from() {
        return from;
    }

    public String to() {
        return to;
    }

    File toFile() {
        return new File(from());
    }
}
