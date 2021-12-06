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

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }
}
