package io.github.therealmone.application;

import com.google.inject.Inject;
import io.github.therealmone.client.Client;
import io.github.therealmone.model.rsa.OpenKey;
import io.github.therealmone.model.rsa.RSA;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigInteger;
import java.util.Arrays;

public class ClientRunner implements Runnable {
    private final static Logger logger = LogManager.getLogger(ClientRunner.class);
    private final Client client;
    private final RSA rsa;

    @Inject
    public ClientRunner(
            final Client client,
            final RSA rsa) {
        this.client = client;
        this.rsa = rsa;
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

            final OpenKey serverKey = client.read(OpenKey.class);
            final String message = "RSA message";
            logger.info("Encrypting message: '{}' with key: [{}, {}]", message, serverKey.getN(), serverKey.getE());
            final BigInteger[] encryptedMessage = rsa.encode(serverKey, message);
            logger.info(Arrays.toString(encryptedMessage));
            client.write(encryptedMessage);

            client.shutDown();
        } catch (Exception e) {
            logger.error("Client throws exception: {}", e);
        }
    }
}
