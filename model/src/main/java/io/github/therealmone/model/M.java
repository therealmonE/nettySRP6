package io.github.therealmone.model;

import java.math.BigInteger;

public interface M extends Element<BigInteger> {
    static M getInstance(final BigInteger value) {
        return (M) () -> value;
    }
}
