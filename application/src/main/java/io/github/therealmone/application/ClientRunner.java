package io.github.therealmone.application;

import com.google.inject.Inject;
import io.github.therealmone.client.Client;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ClientRunner implements Runner {
    private final static Logger logger = LogManager.getLogger(ClientRunner.class);
    private final Client client;
    private boolean done = false;

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

            //client.shutDown();
            done = true;

        } catch (Exception e) {
            logger.error("Client throws exception: {}", e);
        }
    }

    @Override
    public boolean isDone() {
        return done;
    }
}
