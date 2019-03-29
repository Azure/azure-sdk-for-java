package com.microsoft.windowsazure.services.media.authentication;

import java.util.Date;

/**
 * Represents an access token
 */
public class AzureAdAccessToken {

    private final String accessToken;

    private final Date expiresOn;

    /**
     * Gets the access token
     * @return the access token
     */
    public String getAccessToken() {
        return this.accessToken;
    }

    /**
     * Gets the expiration date
     * @return the expiration date
     */
    public Date getExpiresOnDate() {
        return this.expiresOn;
    }

    /**
     * Instantiate a representation of an access token
     * @param accessToken the access token
     * @param expiresOn the expiration date
     */
    public AzureAdAccessToken(String accessToken, Date expiresOn) {

        if (accessToken == null || accessToken.trim().isEmpty()) {
            throw new IllegalArgumentException("accessToken");
        }

        if (expiresOn == null) {
            throw new NullPointerException("expiresOn");
        }

        this.accessToken = accessToken;
        this.expiresOn = expiresOn;
    }
}
