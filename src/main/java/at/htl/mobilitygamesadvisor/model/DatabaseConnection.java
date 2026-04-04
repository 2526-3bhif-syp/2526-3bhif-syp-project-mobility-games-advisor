package at.htl.mobilitygamesadvisor.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL  = "jdbc:postgresql://localhost:5432/mobilitygames";
    private static final String USER = "admin";
    private static final String PASS = "admin";

    private static Connection instance;

    public static Connection get() throws SQLException {
        if (instance == null || instance.isClosed()) {
            instance = DriverManager.getConnection(URL, USER, PASS);
        }
        return instance;
    }
}