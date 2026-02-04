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
     * Stores the access token.
     */
    private final String accessToken;

    /**
     * Stores a flag indicating if challenge resource verification shall be disabled.
     */
    private boolean disableChallengeResourceVerification = false;

    /**
     * Constructor.
     *
     * @param keyVaultUri The Azure Key Vault URI.
     */
    public KeyVaultLoadStoreParameter(String keyVaultUri) {
        this(new Builder(keyVaultUri));
    }

    /**
     * Constructor.
     *
     * @param keyVaultUri The Azure Key Vault URI.
     * @param managedIdentity The managed identity.
     */
    public KeyVaultLoadStoreParameter(String keyVaultUri, String managedIdentity) {
        this(new Builder(keyVaultUri).managedIdentity(managedIdentity));
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
        this(new Builder(keyVaultUri).tenantId(tenantId).clientId(clientId).clientSecret(clientSecret));
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
        this(new Builder(keyVaultUri).tenantId(tenantId)
            .clientId(clientId)
            .clientSecret(clientSecret)
            .managedIdentity(managedIdentity));
    }

    /**
     * Private constructor used by the builder.
     *
     * @param builder The builder instance.
     */
    private KeyVaultLoadStoreParameter(Builder builder) {
        this.keyVaultUri = builder.keyVaultUri;
        this.tenantId = builder.tenantId;
        this.clientId = builder.clientId;
        this.clientSecret = builder.clientSecret;
        this.managedIdentity = builder.managedIdentity;
        this.accessToken = builder.accessToken;
        this.disableChallengeResourceVerification = builder.disableChallengeResourceVerification;
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
     * Get the access token.
     *
     * @return The access token.
     */
    String getAccessToken() {
        return accessToken;
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
    boolean isChallengeResourceVerificationDisabled() {
        return disableChallengeResourceVerification;
    }

    /**
     * Disables verifying if the authentication challenge resource matches the Key Vault or Managed HSM domain. This
     * verification is performed by default.
     */
    public void disableChallengeResourceVerification() {
        disableChallengeResourceVerification = true;
    }

    /**
     * Creates a new builder instance for constructing KeyVaultLoadStoreParameter.
     *
     * @param keyVaultUri The Azure Key Vault URI (required).
     * @return A new builder instance.
     */
    public static Builder builder(String keyVaultUri) {
        return new Builder(keyVaultUri);
    }

    /**
     * Builder class for constructing KeyVaultLoadStoreParameter instances with a fluent API.
     * This provides a clearer and more maintainable way to create instances compared to
     * multiple overloaded constructors.
     */
    public static final class Builder {
        private final String keyVaultUri;
        private String tenantId;
        private String clientId;
        private String clientSecret;
        private String managedIdentity;
        private String accessToken;
        private boolean disableChallengeResourceVerification = false;

        /**
         * Creates a new builder with the required Key Vault URI.
         *
         * @param keyVaultUri The Azure Key Vault URI (required).
         */
        private Builder(String keyVaultUri) {
            if (keyVaultUri == null) {
                throw new IllegalArgumentException("keyVaultUri cannot be null");
            }
            this.keyVaultUri = keyVaultUri;
        }

        /**
         * Sets the tenant id for authentication.
         *
         * @param tenantId The tenant id.
         * @return This builder instance.
         */
        public Builder tenantId(String tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        /**
         * Sets the client id for authentication.
         *
         * @param clientId The client id.
         * @return This builder instance.
         */
        public Builder clientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        /**
         * Sets the client secret for authentication.
         *
         * @param clientSecret The client secret.
         * @return This builder instance.
         */
        public Builder clientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
            return this;
        }

        /**
         * Sets the managed identity for authentication.
         *
         * @param managedIdentity The user-assigned managed identity.
         * @return This builder instance.
         */
        public Builder managedIdentity(String managedIdentity) {
            this.managedIdentity = managedIdentity;
            return this;
        }

        /**
         * Sets the access token for authentication.
         *
         * @param accessToken The access token.
         * @return This builder instance.
         */
        public Builder accessToken(String accessToken) {
            this.accessToken = accessToken;
            return this;
        }

        /**
         * Disables verifying if the authentication challenge resource matches the Key Vault or
         * Managed HSM domain. This verification is performed by default.
         *
         * @return This builder instance.
         */
        public Builder disableChallengeResourceVerification() {
            this.disableChallengeResourceVerification = true;
            return this;
        }

        /**
         * Builds and returns a new KeyVaultLoadStoreParameter instance with the configured values.
         *
         * @return A new KeyVaultLoadStoreParameter instance.
         */
        public KeyVaultLoadStoreParameter build() {
            return new KeyVaultLoadStoreParameter(this);
        }
    }
}
