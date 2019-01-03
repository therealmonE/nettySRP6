package io.github.therealmone.model;

import java.math.BigInteger;

public interface K extends Element<BigInteger> {
    static K getInstance(final BigInteger value) {
        return (K) () -> value;
    }
}
