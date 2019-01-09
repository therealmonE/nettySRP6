package io.github.therealmone.model.rsa;


import com.typesafe.config.Config;

import javax.inject.Named;
import java.math.BigInteger;

public class RSA {
    private final OpenKey openKey;
    private final CloseKey closeKey;

    public RSA(@Named("RSAConfig") final Config rsaConfig) {
        final BigInteger p = BigInteger.valueOf(rsaConfig.getInt("P"));
        final BigInteger q = BigInteger.valueOf(rsaConfig.getInt("Q"));
        final BigInteger n = p.multiply(q); //n = p * q
        final BigInteger phi = p.subtract(BigInteger.valueOf(1)).multiply(q.subtract(BigInteger.valueOf(1))); //phi = (p - 1) * (q - 1)
        final BigInteger e = calculateE(phi);
        final BigInteger d = calculateD(e, phi);
        this.openKey = new OpenKey(n, e);
        this.closeKey = new CloseKey(n, d);
    }

    public BigInteger[] encode(final String message) {
        final char[] chars = message.toCharArray();
        final BigInteger[] encodedMessage = new BigInteger[chars.length];
        for (int i = 0; i < chars.length; i++) {
            final BigInteger encodedChar = BigInteger.valueOf((int) chars[i]).pow(openKey.getE().intValue()).mod(openKey.getN());
            encodedMessage[i] = encodedChar;
        }
        return encodedMessage;
    }

    public String decode(final BigInteger[] message) {
        final StringBuilder decodedMessage = new StringBuilder();
        for (final BigInteger code : message) {
            final BigInteger decodedChar = code.pow(closeKey.getD().intValue()).mod(closeKey.getN());
            decodedMessage.append((char) decodedChar.intValue());
        }
        return decodedMessage.toString();
    }

    private BigInteger calculateE(final BigInteger phi) {
        BigInteger e = phi.subtract(BigInteger.valueOf(1));
        for (long i = 2; i < phi.longValue(); i++) {
            if(phi.divide(BigInteger.valueOf(i)).compareTo(BigInteger.valueOf(0)) == 0
                && e.divide(BigInteger.valueOf(i)).compareTo(BigInteger.valueOf(0)) == 0) {
                e = e.subtract(BigInteger.valueOf(1));
                i = 1;
            }
        }
        return e;
    }

    private BigInteger calculateD(final BigInteger e, final BigInteger phi) {
        BigInteger d = BigInteger.valueOf(10);
        while(e.multiply(d).mod(phi).compareTo(BigInteger.valueOf(1)) != 0) {
            d = d.add(BigInteger.valueOf(1));
        }
        return d;
    }

    private class CloseKey {
        private final BigInteger n;
        private final BigInteger d;

        CloseKey(final BigInteger n, final BigInteger d) {
            this.n = n;
            this.d = d;
        }

        public BigInteger getN() {
            return n;
        }

        public BigInteger getD() {
            return d;
        }
    }

    private class OpenKey {
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
}
