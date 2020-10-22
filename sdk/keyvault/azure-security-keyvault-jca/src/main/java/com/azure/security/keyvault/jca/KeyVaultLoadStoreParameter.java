// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import java.security.KeyStore;

/**
 * The Azure KeyVault LoadStoreParameter of the KeyStoreSpi.
 */
public class KeyVaultLoadStoreParameter implements KeyStore.LoadStoreParameter {

    /**
     * Stores the URI.
     */
    private final String uri;

    /**
     * Stores the tenant id.
     */
    private final String tenantId;

    /**
     * Stores the client ID.
     */
    private final String clientId;

    /**
     * Stores the client secret.
     */
    private final String clientSecret;

    /**
     * Constructor.
     *
     * @param uri the KeyVault URI.
     * @param tenantId the tenant ID.
     * @param clientId the client ID.
     * @param clientSecret the client secret.
     */
    public KeyVaultLoadStoreParameter(String uri, String tenantId, String clientId, String clientSecret) {
        this.uri = uri;
        this.tenantId = tenantId;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    /**
     * Get the protection parameter.
     *
     * @return null
     */
    @Override
    public KeyStore.ProtectionParameter getProtectionParameter() {
        return null;
    }

    /**
     * Get the client id.
     *
     * @return the client id.
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Get the client secret.
     *
     * @return the client secret.
     */
    public String getClientSecret() {
        return clientSecret;
    }

    /**
     * Get the tenant id.
     *
     * @return the tenant id.
     */
    public String getTenantId() {
        return tenantId;
    }

    /**
     * Get the uri.
     *
     * @return the URI.
     */
    public String getUri() {
        return uri;
    }
}
