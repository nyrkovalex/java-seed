package com.github.nyrkovalex.seed.core.flow;

public class FlowInterruptedException extends Exception {
    public FlowInterruptedException(String s, Throwable cause) {
        super(String.format("Flow interrupted. Step %s failed", s), cause);
    }
}
