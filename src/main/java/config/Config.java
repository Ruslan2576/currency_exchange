package config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class Config {
    private static final Properties properties = new Properties();

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (InputStream is = Config.class.getClassLoader().getResourceAsStream("application.properties")) {

                if (is == null) {
                    throw new RuntimeException("application.properties not found in classpath");
                }

                properties.load(is);
            }
        } catch (ClassNotFoundException | IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        String url = properties.getProperty("db.url");
        String username = properties.getProperty("db.username");
        String password = properties.getProperty("db.password");

        if (url == null || username == null || password == null) {
            throw new SQLException("Missing database configuration");
        }

        return DriverManager.getConnection(url, username, password);
    }
}
