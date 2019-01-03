package io.github.therealmone.model;

import java.math.BigInteger;

public interface G extends Element<BigInteger> {
    static G getInstance(final BigInteger value) {
        return (G) () -> value;
    }
}
