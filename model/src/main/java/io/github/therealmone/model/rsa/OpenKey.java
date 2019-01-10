package io.github.therealmone.model.rsa;

import java.io.Serializable;
import java.math.BigInteger;

public class OpenKey implements Serializable {
    private final BigInteger n;
    private final BigInteger e;

    OpenKey(final BigInteger n, final BigInteger e) {
        this.n = n;
        this.e = e;
    }

    public BigInteger getN() {
        return n;
    }

    public BigInteger getE() {
        return e;
    }
}
