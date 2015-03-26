
package com.github.nyrkovalex.seed;


class IoFs implements Io.Fs {

    private static final IoFs INSTANCE = new IoFs();

    public static IoFs instance() {
        return INSTANCE;
    }

    private IoFs() { }

    @Override
    public Io.File file(String path) {
        return new IoFile(path);
    }
}


