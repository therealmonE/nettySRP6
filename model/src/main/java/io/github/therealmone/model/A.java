package io.github.therealmone.model;

import java.math.BigInteger;

public interface A extends Element<BigInteger> {
    static A getInstance(final BigInteger value) {
        return (A) () -> value;
    }
}
