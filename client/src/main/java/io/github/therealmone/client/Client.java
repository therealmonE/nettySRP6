package io.github.therealmone.client;

public interface Client {
    void run() throws Exception;
    void shutDown() throws Exception;
    boolean connected();
    void register(String username, String password) throws Exception;
    void login(String username, String password) throws Exception;
    void write(Object message) throws Exception;
    <T> T read(Class<T> clazz) throws Exception;
}
