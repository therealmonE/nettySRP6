package io.github.therealmone.server;

import java.math.BigInteger;

public class UserContext {
    private final String username;
    private final String salt;
    private final BigInteger passwordVerifier;

    public UserContext(
            final String username,
            final String salt,
            final BigInteger passwordVerifier) {
        this.username = username;
        this.salt = salt;
        this.passwordVerifier = passwordVerifier;
    }

    public String getUsername() {
        return username;
    }

    public String getSalt() {
        return salt;
    }

    public BigInteger getPasswordVerifier() {
        return passwordVerifier;
    }

    @Override
    public String toString() {
        return "UserContext{" +
                "username='" + username + '\'' +
                ", salt='" + salt + '\'' +
                ", passwordVerifier=" + passwordVerifier +
                '}';
    }
}
