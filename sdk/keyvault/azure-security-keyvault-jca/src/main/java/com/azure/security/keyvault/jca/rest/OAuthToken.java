// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.jca.rest;

import java.io.Serializable;

/**
 * An OAuth2 token.
 */
public class OAuthToken implements Serializable {

    /**
     * Stores the access token.
     */
    private String access_token;

    /**
     * Get the access token.
     *
     * @return the access token.
     */
    public String getAccess_token() {
        return access_token;
    }

    /**
     * Set the access token.
     *
     * @param accessToken the access token.
     */
    public void setAccess_token(String accessToken) {
        this.access_token = accessToken;
    }
}
