// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.jca.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/**
 * An OAuth2 token.
 */
public class OAuthToken implements Serializable {
    
    /**
     * Stores the serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Stores the access token.
     */
    @JsonProperty("access_token")
    private String accessToken;

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
}
