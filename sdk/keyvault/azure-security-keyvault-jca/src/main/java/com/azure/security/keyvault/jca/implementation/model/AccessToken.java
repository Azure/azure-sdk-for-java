// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.jca.implementation.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

/**
 * An OAuth2 token.
 */
public class AccessToken {

    /**
     * Stores the access token.
     */
    @JsonProperty("access_token")
    private String accessToken;

    /**
     * Stores the life duration of the access token.
     */
    @JsonProperty("expires_in")
    private long expiresIn;

    /**
     *
     * @return the life duration of the access token in seconds
     */
    public long getExpiresIn() {
        return expiresIn;
    }

    /**
     * Set the life duration of the access token in seconds
     *
     * @param expiresIn
     */
    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }

    /**
     * Stores the time when the token is retrieved for the first time.
     */
    private final OffsetDateTime creationDate = OffsetDateTime.now();

    /**
     * Get the access token.
     *
     * @return the access token.
     */
    public String getAccessToken() {
        return accessToken;
    }

    /**
     * Set the access token.
     *
     * @param accessToken the access token.
     */
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    /**
     * Reserve 60 seconds, in case that the time the token is used it is valid but when the token gets to the server side, it expires.
     * @return boolean, whether the token is expired.
     *
     */
    public boolean isExpired() {
        return OffsetDateTime.now().isAfter(creationDate.plusSeconds(expiresIn - 60));
    }
}
