package com.microsoft.windowsazure.services.media.authentication;

/**
 * Represents a pair of username &amp; password credentials
 */
public class AzureAdClientUsernamePassword {

    private final String username;
    private final String password;

    /**
     * Gets the username.
     * @return the username.
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * Gets the password.
     * @return the password.
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * Initializes a new instance of the AzureAdClientSymmetricKey class.
     * @param username The client username
     * @param password The client password
     */
    public AzureAdClientUsernamePassword(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("username");
        }

        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("password");
        }

        this.username = username;
        this.password = password;
    }
}
