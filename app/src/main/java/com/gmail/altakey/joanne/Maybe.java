package com.gmail.altakey.joanne;

public class Maybe<T> {
    private final T mTarget;

    private Maybe(T target) {
        mTarget = target;
    }

    public static <T> Maybe<T> of(T o) {
        return new Maybe<>(o);
    }

    public T get() throws Nothing {
        if (mTarget != null) {
            return mTarget;
        } else {
            throw new Nothing();
        }
    }

    public static class Nothing extends Exception {
    }
}
