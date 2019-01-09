package io.github.therealmone.application;


import com.google.inject.Inject;
import io.github.therealmone.server.Server;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerRunner implements Runnable {
    private final static Logger logger = LogManager.getLogger(ServerRunner.class);
    private final Server server;

    @Inject
    public ServerRunner(final Server server) {
        this.server = server;
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

            server.shutDown();
        } catch (Exception e) {
            logger.error("Server throws exception");
        }
    }
}
