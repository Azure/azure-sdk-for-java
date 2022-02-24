// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aadb2c.properties;

/**
 * Properties used for authorize.
 */
public class AadB2cCredentialProperties {

    /**
     * Client id to use when performing service principal authentication with Azure.
     */
    private String clientId;

    /**
     * Client secret to use when performing service principal authentication with Azure.
     */
    private String clientSecret;

    /**
     *
     * @return The client id.
     */
    public String getClientId() {
        return clientId;
    }

    /**
     *
     * @param clientId The client id.
     */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /**
     *
     * @return The client secret.
     */
    public String getClientSecret() {
        return clientSecret;
    }

    /**
     *
     * @param clientSecret The client secret.
     */
    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }
}
