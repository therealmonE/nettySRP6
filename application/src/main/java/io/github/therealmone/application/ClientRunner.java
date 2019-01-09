package io.github.therealmone.application;

import com.google.inject.Inject;
import io.github.therealmone.client.Client;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ClientRunner implements Runnable {
    private final static Logger logger = LogManager.getLogger(ClientRunner.class);
    private final Client client;

    @Inject
    public ClientRunner(final Client client) {
        this.client = client;
    }

    @Override
    public void run() {
        try {
            client.run();

            logger.info("Waiting for connection...");
            while(!client.connected()) {
                Thread.sleep(1000);
            }
            logger.info("Got connection");

            client.register("login", "password");
            logger.info("Server message: {}", client.read(String.class));
            client.login("login", "password");

            client.shutDown();
        } catch (Exception e) {
            logger.error("Client throws exception: {}", e);
        }
    }
}
