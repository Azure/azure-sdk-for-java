// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import java.security.KeyStore;

/**
 * The Azure Key Vault LoadStoreParameter of the KeyStoreSpi.
 */
public class KeyVaultLoadStoreParameter implements KeyStore.LoadStoreParameter {

    /**
     * Stores the URI.
     */
    private final String uri;

    /**
     * Stores the Azure AD authentication URL.
     */
    private final String aadAuthenticationUrl;

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
     * Stores the user-assigned identity.
     */
    private final String managedIdentity;

    /**
     * Constructor.
     *
     * @param uri the Azure Key Vault URI.
     */
    public KeyVaultLoadStoreParameter(String uri) {
        this(uri, null);
    }

    /**
     * Constructor.
     *
     * @param uri the Azure Key Vault URI.
     * @param managedIdentity the managed identity.
     */
    public KeyVaultLoadStoreParameter(String uri, String managedIdentity) {
        this.uri = uri;
        this.aadAuthenticationUrl = null;
        this.tenantId = null;
        this.clientId = null;
        this.clientSecret = null;
        this.managedIdentity = managedIdentity;
    }

    /**
     * Constructor.
     *
     * @param uri the Azure Key Vault URI.
     * @param tenantId the tenant ID.
     * @param clientId the client ID.
     * @param clientSecret the client secret.
     */
    public KeyVaultLoadStoreParameter(String uri, String tenantId, String clientId, String clientSecret) {
        this(uri, null, tenantId, clientId, clientSecret);
    }


    /**
     * Constructor.
     *
     * @param uri the Azure Key Vault URI.
     * @param aadAuthenticationUrl the Azure AD authentication URL.
     * @param tenantId the tenant ID.
     * @param clientId the client ID.
     * @param clientSecret the client secret.
     */
    public KeyVaultLoadStoreParameter(String uri, String aadAuthenticationUrl,
            String tenantId, String clientId, String clientSecret) {
        this.uri = uri;
        this.aadAuthenticationUrl = aadAuthenticationUrl;
        this.tenantId = tenantId;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.managedIdentity = null;
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
     * Get the Azure AD authentication URL.
     *
     * @return the Azure AD authentication URL.
     */
    public String getAadAuthenticationUrl() {
        return aadAuthenticationUrl;
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
     * Get the managed identity.
     *
     * @return the managed identity.
     */
    public String getManagedIdentity() {
        return managedIdentity;
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
