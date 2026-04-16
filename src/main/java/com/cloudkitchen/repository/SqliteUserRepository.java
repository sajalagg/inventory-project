package com.cloudkitchen.repository;

import com.cloudkitchen.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

public class SqliteUserRepository {
    public Optional<User> findByUsername(Connection connection, String username) throws SQLException {
        String sql = """
                SELECT id, username, password, full_name, role
                FROM users
                WHERE username = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(new User(
                        resultSet.getLong("id"),
                        resultSet.getString("username"),
                        resultSet.getString("password"),
                        resultSet.getString("full_name"),
                        User.Role.valueOf(resultSet.getString("role"))));
            }
        }
    }

    public long insertCustomer(Connection connection, String username, String password, String fullName) throws SQLException {
        String sql = """
                INSERT INTO users (username, password, full_name, role)
                VALUES (?, ?, ?, 'CUSTOMER')
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, username);
            statement.setString(2, password);
            statement.setString(3, fullName);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
                throw new SQLException("User ID was not generated.");
            }
        }
    }
}
