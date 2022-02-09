// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.implementation.util.IdentityConstants;
import com.azure.identity.implementation.util.ValidationUtil;

/**
 * Fluent credential builder for instantiating a {@link InteractiveBrowserCredential}.
 *
 * @see InteractiveBrowserCredential
 */
public class InteractiveBrowserCredentialBuilder extends AadCredentialBuilderBase<InteractiveBrowserCredentialBuilder> {
    private Integer port;
    private boolean automaticAuthentication = true;
    private String redirectUrl;
    private String loginHint;

    /**
     * Sets the port for the local HTTP server, for which {@code http://localhost:{port}} must be
     * registered as a valid reply URL on the application.
     *
     * @deprecated Configure the redirect URL as {@code http://localhost:{port}} via
     * {@link InteractiveBrowserCredentialBuilder#redirectUrl(String)} instead.
     *
     * @param port the port on which the credential will listen for the browser authentication result
     * @return the InteractiveBrowserCredentialBuilder itself
     */
    @Deprecated
    public InteractiveBrowserCredentialBuilder port(int port) {
        this.port = port;
        return this;
    }

    /**
     * Allows to use an unprotected file specified by <code>cacheFileLocation()</code> instead of
     * Gnome keyring on Linux. This is restricted by default.
     *
     * @return An updated instance of this builder.
     */
    InteractiveBrowserCredentialBuilder allowUnencryptedCache() {
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
    InteractiveBrowserCredentialBuilder enablePersistentCache() {
        this.identityClientOptions.enablePersistentCache();
        return this;
    }


    /**
     * Sets the {@link AuthenticationRecord} captured from a previous authentication.
     *
     * @param authenticationRecord The Authentication record to be configured.
     *
     * @return An updated instance of this builder with the configured authentication record.
     */
    public InteractiveBrowserCredentialBuilder authenticationRecord(AuthenticationRecord authenticationRecord) {
        this.identityClientOptions.setAuthenticationRecord(authenticationRecord);
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
    public InteractiveBrowserCredentialBuilder tokenCachePersistenceOptions(TokenCachePersistenceOptions
                                                                          tokenCachePersistenceOptions) {
        this.identityClientOptions.setTokenCacheOptions(tokenCachePersistenceOptions);
        return this;
    }


    /**
     * Sets the Redirect URL where STS will callback the application with the security code. It is required if a custom
     * client id is specified via {@link InteractiveBrowserCredentialBuilder#clientId(String)} and must match the
     * redirect URL specified during the application registration.
     *
     * @param redirectUrl the redirect URL to listen on and receive security code.
     *
     * @return An updated instance of this builder with the configured redirect URL.
     */
    public InteractiveBrowserCredentialBuilder redirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
        return this;
    }

    /**
     * Disables the automatic authentication and prevents the {@link InteractiveBrowserCredential} from automatically
     * prompting the user. If automatic authentication is disabled a {@link AuthenticationRequiredException}
     * will be thrown from {@link InteractiveBrowserCredential#getToken(TokenRequestContext)} in the case that
     * user interaction is necessary. The application is responsible for handling this exception, and
     * calling {@link InteractiveBrowserCredential#authenticate()} or
     * {@link InteractiveBrowserCredential#authenticate(TokenRequestContext)} to authenticate the user interactively.
     *
     * @return An updated instance of this builder with automatic authentication disabled.
     */
    public InteractiveBrowserCredentialBuilder disableAutomaticAuthentication() {
        this.automaticAuthentication = false;
        return this;
    }

    /**
     * Sets the username suggestion to pre-fill the login page's username/email address field. A user may still log in
     * with a different username.
     *
     * @param loginHint the username suggestion to pre-fill the login page's username/email address field.
     *
     * @return An updated instance of this builder with login hint configured.
     */
    public InteractiveBrowserCredentialBuilder loginHint(String loginHint) {
        this.loginHint = loginHint;
        return this;
    }

    /**
     * Creates a new {@link InteractiveBrowserCredential} with the current configurations.
     *
     * @return a {@link InteractiveBrowserCredential} with the current configurations.
     */
    public InteractiveBrowserCredential build() {
        ValidationUtil.validateInteractiveBrowserRedirectUrlSetup(getClass().getSimpleName(), port, redirectUrl);

        String clientId = this.clientId != null ? this.clientId : IdentityConstants.DEVELOPER_SINGLE_SIGN_ON_ID;
        return new InteractiveBrowserCredential(clientId, tenantId, port, redirectUrl, automaticAuthentication,
            loginHint, identityClientOptions);
    }
}
