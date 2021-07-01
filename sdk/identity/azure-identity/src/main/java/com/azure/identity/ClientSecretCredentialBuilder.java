// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.identity.implementation.util.ValidationUtil;

import java.util.HashMap;

/**
 * Fluent credential builder for instantiating a {@link ClientSecretCredential}.
 *
 * @see ClientSecretCredential
 */
public class ClientSecretCredentialBuilder extends AadCredentialBuilderBase<ClientSecretCredentialBuilder> {
    private String clientSecret;

    /**
     * Sets the client secret for the authentication.
     * @param clientSecret the secret value of the AAD application.
     * @return An updated instance of this builder.
     */
    public ClientSecretCredentialBuilder clientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
        return this;
    }

    /**
     * Enables the shared token cache which is disabled by default. If enabled, the credential will store tokens
     * in a cache persisted to the machine, protected to the current user, which can be shared by other credentials
     * and processes.
     *
     * @return An updated instance of this builder.
     */
    ClientSecretCredentialBuilder enablePersistentCache() {
        this.identityClientOptions.enablePersistentCache();
        return this;
    }

    /**
     * Allows to use an unprotected file specified by <code>cacheFileLocation()</code> instead of
     * Gnome keyring on Linux. This is restricted by default.
     *
     * @return An updated instance of this builder.
     */
    ClientSecretCredentialBuilder allowUnencryptedCache() {
        this.identityClientOptions.setAllowUnencryptedCache(true);
        return this;
    }

    /**
     * Configures the persistent shared token cache options and enables the persistent token cache which is disabled
     * by default. If configured, the credential will store tokens in a cache persisted to the machine, protected to
     * the current user, which can be shared by other credentials and processes.
     *
     * @param tokenCachePersistenceOptions the token cache configuration options
     * @return An updated instance of this builder with the token cache options configured.
     */
    public ClientSecretCredentialBuilder tokenCachePersistenceOptions(TokenCachePersistenceOptions
                                                                                tokenCachePersistenceOptions) {
        this.identityClientOptions.setTokenCacheOptions(tokenCachePersistenceOptions);
        return this;
    }

    /**
     * Specifies either the specific regional authority, or use {@link RegionalAuthority#AUTO_DISCOVER_REGION} to
     * attempt to auto-detect the region. If unset, a non-regional authority will be used. This argument should be used
     * only by applications deployed to Azure VMs.
     *
     * @param regionalAuthority the regional authority
     * @return An updated instance of this builder with the regional authority configured.
     */
    public ClientSecretCredentialBuilder regionalAuthority(RegionalAuthority regionalAuthority) {
        this.identityClientOptions.setRegionalAuthority(regionalAuthority);
        return this;
    }

    /**
     * Creates a new {@link ClientCertificateCredential} with the current configurations.
     *
     * @return a {@link ClientSecretCredentialBuilder} with the current configurations.
     */
    public ClientSecretCredential build() {
        ValidationUtil.validate(getClass().getSimpleName(), new HashMap<String, Object>() {{
                put("clientId", clientId);
                put("tenantId", tenantId);
                put("clientSecret", clientSecret);
            }});
        return new ClientSecretCredential(tenantId, clientId, clientSecret, identityClientOptions);
    }
}
