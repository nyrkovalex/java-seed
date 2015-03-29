
package com.github.nyrkovalex.seed;

import java.util.Optional;

class SeedError<T extends Throwable> implements Seed.Error<T> {
    private final Class<T> errClass;
    private Optional<T> err;

    public SeedError(Class<T> errClass) {
        this.errClass = errClass;
        err = Optional.empty();
    }

    @Override
    public void rethrow() throws T {
        if (err.isPresent()) {
            throw err.get();
        }
    }

    @Override
    public void propagate(T err) throws IllegalStateException {
        if (this.err.isPresent()) {
            throw new IllegalStateException(
                    "Error already set, cannot propagate more than one exception");
        }
        this.err = Optional.of(err);
    }

    @Override
    public void safeCall(UnsafeCallable callable) {
        try {
            callable.call();
        } catch (Throwable t) {
            if (!errClass.isInstance(t)) {
                throw new RuntimeException(t);
            }
            propagate(errClass.cast(t));
        }
    }

}
