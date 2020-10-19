// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.rest;

import java.io.Serializable;

/**
 * An OAuth2 token.
 *
 * @author Manfred Riem (manfred.riem@microsoft.com)
 */
public class OAuthToken implements Serializable {

    /**
     * Stores the access token.
     */
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
