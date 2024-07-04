// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import java.security.KeyStore;

/**
 * The Azure Key Vault LoadStoreParameter of the KeyStoreSpi.
 *
 * @see KeyStore.LoadStoreParameter
 */
public final class KeyVaultLoadStoreParameter implements KeyStore.LoadStoreParameter {
    /**
     * Stores the Key Vault URI.
     */
    private final String keyVaultUri;

    /**
     * Stores the tenant id.
     */
    private final String tenantId;

    /**
     * Stores the client id.
     */
    private final String clientId;

    /**
     * Stores the client secret.
     */
    private final String clientSecret;

    /**
     * Stores the user-assigned Managed Identity.
     */
    private final String managedIdentity;

    /**
     * Stores a flag indicating if challenge resource verification shall be disabled.
     */
    private boolean disableChallengeResourceVerification;

    /**
     * Constructor.
     *
     * @param keyVaultUri The Azure Key Vault URI.
     */
    public KeyVaultLoadStoreParameter(String keyVaultUri) {
        this(keyVaultUri, null, null, null, null);
    }

    /**
     * Constructor.
     *
     * @param keyVaultUri The Azure Key Vault URI.
     * @param managedIdentity The managed identity.
     */
    public KeyVaultLoadStoreParameter(String keyVaultUri, String managedIdentity) {
        this(keyVaultUri, null, null, null, managedIdentity);
    }

    /**
     * Constructor.
     *
     * @param keyVaultUri The Azure Key Vault URI.
     * @param tenantId The tenant id.
     * @param clientId The client id.
     * @param clientSecret The client secret.
     */
    public KeyVaultLoadStoreParameter(String keyVaultUri, String tenantId, String clientId, String clientSecret) {
        this(keyVaultUri, tenantId, clientId, clientSecret, null);
    }

    /**
     * Constructor.
     *
     * @param keyVaultUri The Azure Key Vault URI.
     * @param tenantId The tenant id.
     * @param clientId The client id.
     * @param clientSecret The client secret.
     * @param managedIdentity The managed identity.
     */
    public KeyVaultLoadStoreParameter(String keyVaultUri, String tenantId, String clientId, String clientSecret,
        String managedIdentity) {

        this.keyVaultUri = keyVaultUri;
        this.tenantId = tenantId;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.managedIdentity = managedIdentity;
    }

    /**
     * Get the protection parameter.
     *
     * @return {@code null}.
     */
    @Override
    public KeyStore.ProtectionParameter getProtectionParameter() {
        return null;
    }

    /**
     * Get the client id.
     *
     * @return The client id.
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Get the client secret.
     *
     * @return The client secret.
     */
    public String getClientSecret() {
        return clientSecret;
    }

    /**
     * Get the Managed Identity.
     *
     * @return The Managed Identity.
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
     * Get the Azure Key Vault URI.
     *
     * @return The Azure Key Vault URI.
     */
    public String getUri() {
        return keyVaultUri;
    }

    /**
     * Get a value indicating a check verifying if the authentication challenge resource matches the Key Vault or
     * Managed HSM domain will be performed. This verification is performed by default.
     *
     * @return A value indicating if challenge resource verification is disabled.
     */
    public boolean isChallengeResourceVerificationDisabled() {
        return disableChallengeResourceVerification;
    }

    /**
     * Disables verifying if the authentication challenge resource matches the Key Vault or Managed HSM domain. This
     * verification is performed by default.
     */
    public void disableChallengeResourceVerification() {
        disableChallengeResourceVerification = true;
    }
}
