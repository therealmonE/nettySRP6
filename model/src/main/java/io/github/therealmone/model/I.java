package io.github.therealmone.model;

public interface I extends Element<String> {
    static I getInstance(final String value) {
        return (I) () -> value;
    }
}
