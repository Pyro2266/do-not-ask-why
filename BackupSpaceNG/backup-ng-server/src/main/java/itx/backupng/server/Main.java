package itx.backupng.server;

import itx.backupng.server.config.ConfigUtils;
import itx.backupng.server.config.Configuration;
import itx.backupng.server.controller.Controller;
import itx.backupng.server.controller.ControllerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Paths;

public class Main {

    final private static Logger LOG = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        try {
            LOG.info("BackupSpaceNG starting ...");
            String configPath = System.getProperty("configuration");
            Configuration configuration = ConfigUtils.load(Paths.get(configPath));
            Controller controller = new ControllerImpl(configuration);
            controller.start();
            Runtime.getRuntime().addShutdownHook(new ShutdownHook(controller));
            LOG.info("BackupSpaceNG started.");
        } catch (IOException e) {
            LOG.error("BackupSpaceNG error: config file not defined !", e);
        } catch (Exception e) {
            LOG.error("BackupSpaceNG start exception: ", e);
        }
    }

    private static class ShutdownHook extends Thread {
        private Controller controller;

        public ShutdownHook(Controller controller) {
            this.controller = controller;
        }

        public void run() {
            LOG.info("Shutting down BackupSpaceNG ...");
            controller.shutdown();
            LOG.info("BackupSpaceNG stopped.");
        }
    }

}
