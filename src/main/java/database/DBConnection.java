package database;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {

    private static final Properties props = new Properties();

    static {
        try (FileInputStream in = new FileInputStream(AppPaths.configFile().toFile())) {
            props.load(in);
        } catch (Exception e) {
            throw new RuntimeException("Cannot load " + AppPaths.configFile()
                    + " (copy db.properties.example there, or start with -Dlearnspace.home=<folder>)", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        String url = System.getProperty("db.url", props.getProperty("db.url"));
        return DriverManager.getConnection(
                url,
                props.getProperty("db.user"),
                props.getProperty("db.password"));
    }
}