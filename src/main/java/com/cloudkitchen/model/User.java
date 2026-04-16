package com.cloudkitchen.model;

public class User {
    public enum Role {
        ADMIN,
        CUSTOMER
    }

    public static class Session {
        private final User user;
        private final Role selectedPortal;

        public Session(User user, Role selectedPortal) {
            this.user = user;
            this.selectedPortal = selectedPortal;
        }

        public User getUser() {
            return user;
        }

        public Role getSelectedPortal() {
            return selectedPortal;
        }

        public boolean isAdminPortal() {
            return selectedPortal == Role.ADMIN;
        }
    }

    private final long id;
    private final String username;
    private final String password;
    private final String fullName;
    private final Role role;

    public User(long id, String username, String password, String fullName, Role role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
    }

    public long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getFullName() {
        return fullName;
    }

    public Role getRole() {
        return role;
    }
}
