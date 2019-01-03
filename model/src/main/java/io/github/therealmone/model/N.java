package io.github.therealmone.model;

import java.math.BigInteger;

public interface N extends Element<BigInteger> {
    static N getInstance(final BigInteger value) {
        return (N) () -> value;
    }
}
