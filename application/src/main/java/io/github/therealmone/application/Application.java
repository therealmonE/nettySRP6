package io.github.therealmone.application;

import com.google.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Named;

public class Application {
    private final static Logger logger = LogManager.getLogger(Application.class);
    private final Runnable clientRunner;
    private final Runnable serverRunner;

    @Inject
    public Application(
            final @Named("ClientRunner") Runnable clientRunner,
            final @Named("ServerRunner") Runnable serverRunner) {
        this.clientRunner = clientRunner;
        this.serverRunner = serverRunner;
    }

    void run() {
        final Thread serverThread = new Thread(serverRunner);
        final Thread clientThread = new Thread(clientRunner);
        logger.info("Running server...");
        serverThread.start();
        logger.info("Running client...");
        clientThread.start();
    }
}
