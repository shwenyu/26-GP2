package cn.edu.zju.dbutils;

import cn.edu.zju.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.function.Consumer;

public class DBUtils {

    private static final Logger log = LoggerFactory.getLogger(DBUtils.class);

    public static Connection getConnection() {
        Connection connection = null;
        AppConfig appConfig = AppConfig.getInstance();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            log.error("MySQL JDBC driver not found", e);
        }
        try {
            connection = DriverManager.getConnection(appConfig.getJdbcUrl()
                    , appConfig.getJdbcUsername()
                    , appConfig.getJdbcPassword());
        } catch (SQLException e) {
            log.error("Failed to connect database via jdbc.url={}", appConfig.getJdbcUrl(), e);
        }
        return connection;
    }

    public static void execSQL(Consumer<Connection> consumer) {
        Connection connection = null;
        try {
            connection = getConnection();
            if (connection == null) {
                throw new IllegalStateException("Database connection is not available, please check app.properties and MySQL status");
            }
            consumer.accept(connection);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    log.warn("Failed to close database connection", e);
                }
            }
        }
    }
}
