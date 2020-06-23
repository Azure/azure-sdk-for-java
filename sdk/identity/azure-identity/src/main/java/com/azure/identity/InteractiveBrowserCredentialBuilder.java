// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.implementation.util.ValidationUtil;

import java.util.HashMap;

/**
 * Fluent credential builder for instantiating a {@link InteractiveBrowserCredential}.
 *
 * @see InteractiveBrowserCredential
 */
public class InteractiveBrowserCredentialBuilder extends AadCredentialBuilderBase<InteractiveBrowserCredentialBuilder> {
    private int port;
    private boolean automaticAuthentication = true;

    /**
     * Sets the port for the local HTTP server, for which {@code http://localhost:{port}} must be
     * registered as a valid reply URL on the application.
     *
     * @param port the port on which the credential will listen for the browser authentication result
     * @return the InteractiveBrowserCredentialBuilder itself
     */
    public InteractiveBrowserCredentialBuilder port(int port) {
        this.port = port;
        return this;
    }

    /**
     * Sets whether to use an unprotected file specified by <code>cacheFileLocation()</code> instead of
     * Gnome keyring on Linux. This is false by default.
     *
     * @param allowUnencryptedCache whether to use an unprotected file for cache storage.
     *
     * @return An updated instance of this builder with the unprotected token cache setting set as specified.
     */
    public InteractiveBrowserCredentialBuilder allowUnencryptedCache(boolean allowUnencryptedCache) {
        this.identityClientOptions.allowUnencryptedCache(allowUnencryptedCache);
        return this;
    }

    /**
     * Sets whether to enable using the shared token cache. This is disabled by default.
     *
     * @param enabled whether to enabled using the shared token cache.
     *
     * @return An updated instance of this builder with if the shared token cache enabled specified.
     */
    public InteractiveBrowserCredentialBuilder enablePersistentCache(boolean enabled) {
        this.identityClientOptions.enablePersistentCache(enabled);
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
     * Creates a new {@link InteractiveBrowserCredential} with the current configurations.
     *
     * @return a {@link InteractiveBrowserCredential} with the current configurations.
     */
    public InteractiveBrowserCredential build() {
        ValidationUtil.validate(getClass().getSimpleName(), new HashMap<String, Object>() {{
                put("clientId", clientId);
                put("port", port);
            }});
        return new InteractiveBrowserCredential(clientId, tenantId, port, automaticAuthentication,
                identityClientOptions);
    }
}
