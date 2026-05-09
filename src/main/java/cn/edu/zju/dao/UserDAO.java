package cn.edu.zju.dao;

import cn.edu.zju.bean.User;
import cn.edu.zju.dbutils.DBUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

public class UserDAO extends BaseDao {

    private static final Logger log = LoggerFactory.getLogger(UserDAO.class);

    public User findByUsername(String username) {
        AtomicReference<User> user = new AtomicReference<>();
        DBUtils.execSQL(connection -> {
            try {
                if (connection == null) {
                    throw new IllegalStateException("Database connection is not available");
                }
                PreparedStatement preparedStatement = connection.prepareStatement("select id, username, password_hash, created_at from user where username = ?");
                preparedStatement.setString(1, username);
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    user.set(mapUser(resultSet));
                }
            } catch (SQLException e) {
                log.info("Failed to query user by username", e);
                throw new IllegalStateException("Failed to query user by username", e);
            }
        });
        return user.get();
    }

    public Long insertUser(String username, String passwordHash) {
        AtomicReference<Long> generatedId = new AtomicReference<>();
        DBUtils.execSQL(connection -> {
            try {
                if (connection == null) {
                    throw new IllegalStateException("Database connection is not available");
                }
                PreparedStatement preparedStatement = connection.prepareStatement(
                        "insert into user(username, password_hash, created_at) values (?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS
                );
                preparedStatement.setString(1, username);
                preparedStatement.setString(2, passwordHash);
                preparedStatement.setTimestamp(3, new Timestamp(new Date().getTime()));
                preparedStatement.executeUpdate();
                ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    generatedId.set(generatedKeys.getLong(1));
                }
            } catch (SQLException e) {
                log.info("Failed to insert user", e);
                throw new IllegalStateException("Failed to insert user", e);
            }
        });
        return generatedId.get();
    }

    public User findById(Long id) {
        AtomicReference<User> user = new AtomicReference<>();
        DBUtils.execSQL(connection -> {
            try {
                if (connection == null) {
                    throw new IllegalStateException("Database connection is not available");
                }
                PreparedStatement preparedStatement = connection.prepareStatement("select id, username, password_hash, created_at from user where id = ?");
                preparedStatement.setLong(1, id);
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    user.set(mapUser(resultSet));
                }
            } catch (SQLException e) {
                log.info("Failed to query user by id", e);
                throw new IllegalStateException("Failed to query user by id", e);
            }
        });
        return user.get();
    }

    private User mapUser(ResultSet resultSet) throws SQLException {
        Long id = resultSet.getLong("id");
        String username = resultSet.getString("username");
        String passwordHash = resultSet.getString("password_hash");
        Timestamp createdAt = resultSet.getTimestamp("created_at");
        return new User(id, username, passwordHash, createdAt == null ? null : new Date(createdAt.getTime()));
    }
}
