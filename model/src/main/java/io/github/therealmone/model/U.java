package io.github.therealmone.model;

import java.math.BigInteger;

public interface U extends Element<BigInteger> {
    static U getInstance(final BigInteger value) {
        return (U) () -> value;
    }
}
