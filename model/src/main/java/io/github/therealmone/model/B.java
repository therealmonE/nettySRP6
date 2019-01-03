package io.github.therealmone.model;

import java.math.BigInteger;

public interface B extends Element<BigInteger> {
    static B getInstance(final BigInteger value) {
        return (B) () -> value;
    }
}
