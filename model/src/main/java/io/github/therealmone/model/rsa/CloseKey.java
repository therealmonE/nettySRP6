package io.github.therealmone.model.rsa;

import java.math.BigInteger;

class CloseKey {
    private final BigInteger n;
    private final BigInteger d;

    CloseKey(final BigInteger n, final BigInteger d) {
        this.n = n;
        this.d = d;
    }

    BigInteger getN() {
        return n;
    }

    BigInteger getD() {
        return d;
    }
}
