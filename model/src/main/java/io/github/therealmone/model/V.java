package io.github.therealmone.model;

import java.math.BigInteger;

public interface V extends Element<BigInteger> {
    static V getInstance(final BigInteger value) {
        return (V) () -> value;
    }
}
