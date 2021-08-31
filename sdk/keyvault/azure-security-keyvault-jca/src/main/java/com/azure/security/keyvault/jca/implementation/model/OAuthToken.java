// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.jca.implementation.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

/**
 * An OAuth2 token.
 */
public class OAuthToken {

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
     * Stores the life duration of the access token.
     */
    @JsonProperty("ext_expires_in")
    private long extExpiresIn;

    /**
     * Stores the starting time of the access token.
     */
    @JsonProperty("expires_on")
    private long expiresOn;

    /**
     * Stores the expiration time of the access token.
     */
    @JsonProperty("not_before")
    private long notBefore;

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
     * @return boolean, whether the token is expired.
     *
     */
    public boolean isExpired() {
        return OffsetDateTime.now().isAfter(creationDate.plusSeconds(expiresIn));
    }
}
