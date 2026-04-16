package com.cloudkitchen.service;

import com.cloudkitchen.db.DatabaseManager;
import com.cloudkitchen.model.User;
import com.cloudkitchen.repository.SqliteUserRepository;
import java.sql.Connection;
import java.sql.SQLException;

public class AuthService {
    private final DatabaseManager databaseManager;
    private final SqliteUserRepository userRepository;

    public AuthService(DatabaseManager databaseManager, SqliteUserRepository userRepository) {
        this.databaseManager = databaseManager;
        this.userRepository = userRepository;
    }

    public User.Session login(String username, String password, User.Role selectedPortal) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new IllegalArgumentException("Username and password are required.");
        }

        try (Connection connection = databaseManager.getConnection()) {
            User user = userRepository.findByUsername(connection, username.trim())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid username or password."));
            if (!user.getPassword().equals(password)) {
                throw new IllegalArgumentException("Invalid username or password.");
            }
            if (selectedPortal == User.Role.ADMIN && user.getRole() != User.Role.ADMIN) {
                throw new IllegalArgumentException("Only admin accounts can open the admin portal.");
            }
            return new User.Session(user, selectedPortal);
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to complete login", exception);
        }
    }

    public void registerCustomer(String fullName, String username, String password) {
        if (fullName == null || fullName.isBlank()
                || username == null || username.isBlank()
                || password == null || password.isBlank()) {
            throw new IllegalArgumentException("Full name, username, and password are required.");
        }
        if (username.trim().length() < 4) {
            throw new IllegalArgumentException("Username must be at least 4 characters long.");
        }
        if (!username.trim().matches("[A-Za-z0-9_]+")) {
            throw new IllegalArgumentException("Username can contain only letters, numbers, and underscore.");
        }
        if (password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long.");
        }
        if (fullName.trim().length() < 3) {
            throw new IllegalArgumentException("Full name must be at least 3 characters long.");
        }

        try (Connection connection = databaseManager.getConnection()) {
            if (userRepository.findByUsername(connection, username.trim()).isPresent()) {
                throw new IllegalArgumentException("That username is already taken.");
            }
            userRepository.insertCustomer(connection, username.trim(), password, fullName.trim());
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to register customer", exception);
        }
    }
}
