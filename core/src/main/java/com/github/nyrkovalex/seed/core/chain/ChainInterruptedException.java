package com.github.nyrkovalex.seed.core.chain;

public class ChainInterruptedException extends Exception {
    public ChainInterruptedException(String s, Throwable cause) {
        super(String.format("Flow interrupted. Step %s failed", s), cause);
    }
}
