package SWBS.mediatracker;

import SWBS.mediatracker.service.ConfigHandler;
import SWBS.mediatracker.service.ConfigUtil;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;

public class SWBCApplication {

    public static void main(String[] args) throws IOException {

        try {
            String propPath = "F:/MeetVR/Task-Tech/SWBC-Media-Tracker-API-Core-Java/src/main/resources/application.properties";
            Properties properties = ConfigUtil.loadProperties(propPath);
            int port = ConfigUtil.getRequiredPort(properties);

            HttpServer httpServer = HttpServer.create(new InetSocketAddress(port), 0);
            httpServer.createContext("/", new ConfigHandler());
            httpServer.setExecutor(null);
            System.out.println("Listening on SERVER" + httpServer);
            httpServer.start();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }


}
