// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.implementation.util.IdentityUtil;
import com.azure.identity.implementation.util.ValidationUtil;

import java.util.Arrays;
import java.util.List;

/**
 * Fluent credential builder for instantiating a {@link UsernamePasswordCredential}.
 *
 * <p>Username password authentication is a common type of authentication flow used by many applications and services,
 * including <a href="https://learn.microsoft.com/entra/fundamentals/">Microsoft Entra ID</a>.
 * With username password authentication, users enter their username and password credentials to sign
 * in to an application or service.
 * The {@link UsernamePasswordCredential} authenticates a public client application and acquires a token using the
 * user credentials that don't require 2FA/MFA (Multi-factored) authentication. For more information refer to the
 * <a href="https://aka.ms/azsdk/java/identity/usernamepasswordcredential/docs">conceptual knowledge and configuration
 * details</a>.</p>
 *
 * <p><strong>Sample: Construct UsernamePasswordCredential</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a {@link UsernamePasswordCredential},
 * using the {@link UsernamePasswordCredentialBuilder} to configure it. The {@code clientId},
 * {@code username} and {@code password} parameters are required to create
 * {@link UsernamePasswordCredential}. Once this credential is created, it may be passed into the
 * builder of many of the Azure SDK for Java client builders as the 'credential' parameter.</p>
 *
 * <!-- src_embed com.azure.identity.credential.usernamepasswordcredential.construct -->
 * <pre>
 * TokenCredential usernamePasswordCredential = new UsernamePasswordCredentialBuilder&#40;&#41;
 *     .clientId&#40;&quot;&lt;your app client ID&gt;&quot;&#41;
 *     .username&#40;&quot;&lt;your username&gt;&quot;&#41;
 *     .password&#40;&quot;&lt;your password&gt;&quot;&#41;
 *     .build&#40;&#41;;
 * </pre>
 * <!-- end com.azure.identity.credential.usernamepasswordcredential.construct -->
 *
 * @see UsernamePasswordCredential
 */
public class UsernamePasswordCredentialBuilder extends AadCredentialBuilderBase<UsernamePasswordCredentialBuilder> {
    private static final ClientLogger LOGGER = new ClientLogger(UsernamePasswordCredentialBuilder.class);
    private static final String CLASS_NAME = UsernamePasswordCredentialBuilder.class.getSimpleName();

    private String username;
    private String password;

    /**
     * Constructs an instance of UsernamePasswordCredentialBuilder.
     */
    public UsernamePasswordCredentialBuilder() {
        super();
    }

    /**
     * Sets the username of the user.
     * @param username the username of the user
     * @return the UserCredentialBuilder itself
     */
    public UsernamePasswordCredentialBuilder username(String username) {
        this.username = username;
        return this;
    }

    /**
     * Sets the password of the user.
     * @param password the password of the user
     * @return the UserCredentialBuilder itself
     */
    public UsernamePasswordCredentialBuilder password(String password) {
        this.password = password;
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
    public UsernamePasswordCredentialBuilder tokenCachePersistenceOptions(TokenCachePersistenceOptions
                                                                          tokenCachePersistenceOptions) {
        this.identityClientOptions.setTokenCacheOptions(tokenCachePersistenceOptions);
        return this;
    }

    /**
     * Allows to use an unprotected file specified by <code>cacheFileLocation()</code> instead of
     * Gnome keyring on Linux. This is restricted by default.
     *
     * @return An updated instance of this builder.
     */
    UsernamePasswordCredentialBuilder allowUnencryptedCache() {
        this.identityClientOptions.setAllowUnencryptedCache(true);
        return this;
    }

    /**
     * Enables the shared token cache which is disabled by default. If enabled, the credential will store tokens
     * in a cache persisted to the machine, protected to the current user, which can be shared by other credentials
     * and processes.
     *
     * @return An updated instance of this builder with if the shared token cache enabled specified.
     */
    UsernamePasswordCredentialBuilder enablePersistentCache() {
        this.identityClientOptions.enablePersistentCache();
        return this;
    }

    /**
     * For multi-tenant applications, specifies additional tenants for which the credential may acquire tokens.
     * Add the wildcard value "*" to allow the credential to acquire tokens for any tenant on which the application is installed.
     * If no value is specified for TenantId this option will have no effect, and the credential will
     * acquire tokens for any requested tenant.
     *
     * @param additionallyAllowedTenants the additionally allowed tenants.
     * @return An updated instance of this builder with the additional tenants configured.
     */
    @Override
    public UsernamePasswordCredentialBuilder additionallyAllowedTenants(String... additionallyAllowedTenants) {
        identityClientOptions
            .setAdditionallyAllowedTenants(IdentityUtil.resolveAdditionalTenants(Arrays.asList(additionallyAllowedTenants)));
        return this;
    }

    /**
     * For multi-tenant applications, specifies additional tenants for which the credential may acquire tokens.
     * Add the wildcard value "*" to allow the credential to acquire tokens for any tenant on which the application is installed.
     * If no value is specified for TenantId this option will have no effect, and the credential will
     * acquire tokens for any requested tenant.
     *
     * @param additionallyAllowedTenants the additionally allowed tenants.
     * @return An updated instance of this builder with the additional tenants configured.
     */
    @Override
    public UsernamePasswordCredentialBuilder additionallyAllowedTenants(List<String> additionallyAllowedTenants) {
        identityClientOptions.setAdditionallyAllowedTenants(IdentityUtil.resolveAdditionalTenants(additionallyAllowedTenants));
        return this;
    }

    /**
     * Creates a new {@link UsernamePasswordCredential} with the current configurations.
     *
     * @return a {@link UsernamePasswordCredential} with the current configurations.
     */
    public UsernamePasswordCredential build() {
        ValidationUtil.validate(CLASS_NAME, LOGGER, "clientId", clientId, "username", username, "password", password);

        return new UsernamePasswordCredential(clientId, tenantId, username, password, identityClientOptions);
    }
}
