package SWBS.mediatracker;

import SWBS.mediatracker.service.InteractionService;
import SWBS.mediatracker.service.ConfigProperties;
import com.sun.net.httpserver.HttpServer;

import java.net.*;
import java.util.Properties;

public class ServerApplication {
    public static void main(String[] args) {
        try {
            Properties properties = ConfigProperties.loadProperties("src/main/resources/application.properties");
            int port = ConfigProperties.getRequiredPort(properties);
            HttpServer httpServer = HttpServer.create(new InetSocketAddress(port), 0);
            httpServer.createContext("/", new InteractionService());
            httpServer.setExecutor(null);
            System.out.println("Listening on port " + port);
            httpServer.start();

        } catch (Exception e) {
            System.err.println("Server failed to start: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
