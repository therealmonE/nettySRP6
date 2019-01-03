package io.github.therealmone.model;

import java.math.BigInteger;

public interface R extends Element<BigInteger> {
    static R getInstance(final BigInteger value) {
        return (R) () -> value;
    }
}
