// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.implementation.util.ValidationUtil;

import java.util.HashMap;
import java.util.function.Consumer;

/**
 * Fluent credential builder for instantiating a {@link DeviceCodeCredential}.
 *
 * @see DeviceCodeCredential
 */
public class DeviceCodeCredentialBuilder extends AadCredentialBuilderBase<DeviceCodeCredentialBuilder> {
    private Consumer<DeviceCodeInfo> challengeConsumer;
    private boolean automaticAuthentication = true;

    /**
     * Sets the consumer to meet the device code challenge.
     *
     * @param challengeConsumer A method allowing the user to meet the device code challenge.
     * @return the InteractiveBrowserCredentialBuilder itself
     */
    public DeviceCodeCredentialBuilder challengeConsumer(
        Consumer<DeviceCodeInfo> challengeConsumer) {
        this.challengeConsumer = challengeConsumer;
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
    public DeviceCodeCredentialBuilder allowUnencryptedCache(boolean allowUnencryptedCache) {
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
    public DeviceCodeCredentialBuilder enablePersistentCache(boolean enabled) {
        this.identityClientOptions.enablePersistentCache(enabled);
        return this;
    }

    /**
     * Sets the {@link AuthenticationRecord} captured from a previous authentication.
     *
     * @param authenticationRecord the authentication record to ser.
     *
     * @return An updated instance of this builder with if the shared token cache enabled specified.
     */
    public DeviceCodeCredentialBuilder authenticationRecord(AuthenticationRecord authenticationRecord) {
        this.identityClientOptions.setAuthenticationRecord(authenticationRecord);
        return this;
    }

    /**
     * Disables the automatic authentication and prevents the {@link DeviceCodeCredential} from automatically
     * prompting the user. If automatic authentication is disabled a {@link AuthenticationRequiredException}
     * will be thrown from {@link DeviceCodeCredential#getToken(TokenRequestContext)} in the case that
     * user interaction is necessary. The application is responsible for handling this exception, and
     * calling {@link DeviceCodeCredential#authenticate()} or
     * {@link DeviceCodeCredential#authenticate(TokenRequestContext)} to authenticate the user interactively.
     *
     * @return An updated instance of this builder with automatic authentication disabled.
     */
    public DeviceCodeCredentialBuilder disableAutomaticAuthentication() {
        this.automaticAuthentication = false;
        return this;
    }

    /**
     * Creates a new {@link DeviceCodeCredential} with the current configurations.
     *
     * @return a {@link DeviceCodeCredential} with the current configurations.
     */
    public DeviceCodeCredential build() {
        ValidationUtil.validate(getClass().getSimpleName(), new HashMap<String, Object>() {{
                put("clientId", clientId);
                put("challengeConsumer", challengeConsumer);
            }});
        return new DeviceCodeCredential(clientId, tenantId, challengeConsumer, automaticAuthentication,
                identityClientOptions);
    }
}
