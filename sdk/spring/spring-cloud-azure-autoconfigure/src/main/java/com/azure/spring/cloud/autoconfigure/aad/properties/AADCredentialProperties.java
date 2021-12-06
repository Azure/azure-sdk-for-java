// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.properties;

/**
 * Properties used for authorize.
 */
public class AADCredentialProperties {

    /**
     * Client id to use when performing service principal authentication with Azure.
     */
    private String clientId;

    /**
     * Client secret to use when performing service principal authentication with Azure.
     */
    private String clientSecret;

    /**
     * Get client id.
     *
     * @return clientId the client id
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Set client id.
     *
     * @param clientId the client id
     */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /**
     * Get client secret.
     *
     * @return clientSecret the client secret
     */
    public String getClientSecret() {
        return clientSecret;
    }

    /**
     * Set client secret.
     *
     * @param clientSecret the client secret
     */
    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }
}
