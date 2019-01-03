package io.github.therealmone.application;


import com.google.inject.Inject;
import io.github.therealmone.server.Server;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerRunner implements Runner {
    private final static Logger logger = LogManager.getLogger(ServerRunner.class);
    private final Server server;
    private boolean done = false;

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

            //server.shutDown();
            done = true;

        } catch (Exception e) {
            logger.error("Server throws exception");
        }
    }

    public boolean isDone() {
        return done;
    }
}
