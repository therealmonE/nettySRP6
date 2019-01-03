package io.github.therealmone.model;

public interface S extends Element<String> {
    static S getInstance(final String value) {
        return (S) () -> value;
    }
}
