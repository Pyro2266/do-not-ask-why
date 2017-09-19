package itx.backupng.server;

import itx.backupng.server.config.ConfigUtils;
import itx.backupng.server.config.Configuration;
import itx.backupng.server.controller.Controller;
import itx.backupng.server.controller.ControllerImpl;
import itx.backupng.server.grpc.GrpcServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Paths;

public class Main {

    final private static Logger LOG = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        try {
            LOG.info("BackupSpaceNG starting server ...");
            String configPath = System.getProperty("configuration");
            if (configPath == null) {
                LOG.error("ERROR: please specify -Dconfiguration=path/to/configuration.json");
                return;
            }
            LOG.info("using config: {}", configPath);
            Configuration configuration = ConfigUtils.load(Paths.get(configPath));
            Controller controller = new ControllerImpl(configuration);
            controller.start();
            GrpcServer grpcServer = new GrpcServer(
                    configuration.getGrpcServer().getHost(),
                    configuration.getGrpcServer().getPort(),
                    controller.getServices());
            grpcServer.start();
            Runtime.getRuntime().addShutdownHook(new ShutdownHook(controller, grpcServer));
            LOG.info("BackupSpaceNG server started.");
            grpcServer.blockUntilShutdown();
        } catch (IOException e) {
            LOG.error("BackupSpaceNG error: config file not defined !", e);
        } catch (Exception e) {
            LOG.error("BackupSpaceNG start exception: ", e);
        }
    }

    private static class ShutdownHook extends Thread {
        private Controller controller;
        private GrpcServer grpcServer;
        public ShutdownHook(Controller controller, GrpcServer grpcServer) {
            this.controller = controller;
            this.grpcServer = grpcServer;
        }
        public void run() {
            LOG.info("Shutting down BackupSpaceNG ...");
            grpcServer.stop();
            controller.shutdown();
            LOG.info("BackupSpaceNG server stopped.");
        }
    }

}
