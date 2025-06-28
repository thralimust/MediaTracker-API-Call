package SWBS.mediatracker.service;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class ConfigProperties {


    public static Properties loadProperties(String filePath) throws Exception {
        Properties pr = new Properties();
        try (InputStream inputStream = new FileInputStream(filePath)) {
            pr.load(inputStream);
        }
        return pr;
    }


    public static int getRequiredPort(Properties properties) {
        String portStr = properties.getProperty("server.port");
        if (portStr == null || portStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Missing 'server.port' in properties file.");
        }

        try {
            return Integer.parseInt(portStr);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("'server.port' must be an integer.", ex);
        }
    }

}
