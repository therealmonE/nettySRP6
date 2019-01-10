package io.github.therealmone.model.rsa;

import com.typesafe.config.Config;

import javax.inject.Named;
import java.math.BigInteger;
import java.util.Random;

public class RSA {
    private final OpenKey openKey;
    private final CloseKey closeKey;
    private final Random random;

    public RSA(@Named("RSAConfig") final Config rsaConfig) {
        this.random = new Random();
        final BigInteger p = BigInteger.valueOf(rsaConfig.getInt("P"));
        final BigInteger q = BigInteger.valueOf(rsaConfig.getInt("Q"));
        final BigInteger n = p.multiply(q); //n = p * q
        final BigInteger phi = p.subtract(BigInteger.valueOf(1)).multiply(q.subtract(BigInteger.valueOf(1))); //phi = (p - 1) * (q - 1)
        final BigInteger e = calculateE(phi, 10);
        final BigInteger d = e.modInverse(phi);
        this.openKey = new OpenKey(n, e);
        this.closeKey = new CloseKey(n, d);
    }

    public BigInteger[] encode(final OpenKey openKey, final String message) {
        final char[] chars = message.toCharArray();
        final BigInteger[] encodedMessage = new BigInteger[chars.length];
        for (int i = 0; i < chars.length; i++) {
            final BigInteger encodedChar = BigInteger.valueOf((int) chars[i]).modPow(openKey.getE(), openKey.getN());
            encodedMessage[i] = encodedChar;
        }
        return encodedMessage;
    }

    public String decode(final BigInteger[] message) {
        final StringBuilder decodedMessage = new StringBuilder();
        for (final BigInteger code : message) {
            final BigInteger decodedChar = code.modPow(closeKey.getD(), closeKey.getN());
            decodedMessage.append((char) decodedChar.intValue());
        }
        return decodedMessage.toString();
    }

    private BigInteger calculateE(final BigInteger phi, final int maxPow) {
        //e = 2 ^ (2 ^ n) + 1
        //n = random(maxPow)
        final int n = random.nextInt(maxPow - 3) + 3;
        BigInteger e = BigInteger.valueOf(2).pow(BigInteger.valueOf(2).pow(n).intValue()).add(BigInteger.valueOf(1));
        while(phi.gcd(e).intValue() > 1) {
            e = e.add(BigInteger.valueOf(2));
        }
        if(e.compareTo(phi) > 0) {
            return calculateE(phi, maxPow - 1);
        } else {
            return e;
        }
    }

    public OpenKey getOpenKey() {
        return openKey;
    }
}
