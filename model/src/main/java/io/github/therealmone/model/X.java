package io.github.therealmone.model;

import java.math.BigInteger;

public interface X extends Element<BigInteger> {
    static X getInstance(final BigInteger value) {
        return (X) () -> value;
    }
}
