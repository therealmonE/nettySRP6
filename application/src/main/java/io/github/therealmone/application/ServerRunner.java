package io.github.therealmone.application;


import com.google.inject.Inject;
import io.github.therealmone.model.rsa.RSA;
import io.github.therealmone.server.Server;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigInteger;
import java.util.Arrays;

public class ServerRunner implements Runnable {
    private final static Logger logger = LogManager.getLogger(ServerRunner.class);
    private final Server server;
    private final RSA rsa;

    @Inject
    public ServerRunner(
            final Server server,
            final RSA rsa) {
        this.server = server;
        this.rsa = rsa;
    }

    @Override
    public void run() {
        try {
            server.run();

            logger.info("Waiting for connection...");
            while(!server.connected()) {
                Thread.sleep(1000);
            }
            logger.info("Got connection");

            server.registerNewUser();
            server.authenticate();

            logger.info("Sending open key [{}, {}], ", rsa.getOpenKey().getN(), rsa.getOpenKey().getE());
            server.write(rsa.getOpenKey());

            final BigInteger[] encryptedMessage = server.read(BigInteger[].class);
            logger.info("Decrypting message: {}", Arrays.toString(encryptedMessage));
            logger.info("Message: '{}'", rsa.decode(encryptedMessage));

            server.shutDown();
        } catch (Exception e) {
            logger.error("Server throws exception: {}", e);
        }
    }
}
