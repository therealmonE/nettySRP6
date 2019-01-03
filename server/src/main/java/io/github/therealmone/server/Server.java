package io.github.therealmone.server;

public interface Server {
    void run() throws Exception;
    void shutDown() throws Exception;
    boolean connected();
    void registerNewUser() throws Exception;
    void authenticate() throws Exception;
    void write(Object message) throws Exception;
    <T> T read(Class<T> clazz) throws Exception;
}
