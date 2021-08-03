// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.implementation.util.IdentityConstants;

import java.util.function.Consumer;

/**
 * Fluent credential builder for instantiating a {@link DeviceCodeCredential}.
 *
 * @see DeviceCodeCredential
 */
public class DeviceCodeCredentialBuilder extends AadCredentialBuilderBase<DeviceCodeCredentialBuilder> {
    private Consumer<DeviceCodeInfo> challengeConsumer =
        deviceCodeInfo -> System.out.println(deviceCodeInfo.getMessage());

    private boolean automaticAuthentication = true;

    /**
     * Sets the consumer to meet the device code challenge. If not specified a default consumer is used which prints
     * the device code info message to stdout.
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
     * Configures the persistent shared token cache options and enables the persistent token cache which is disabled
     * by default. If configured, the credential will store tokens in a cache persisted to the machine, protected to
     * the current user, which can be shared by other credentials and processes.
     *
     * @param tokenCachePersistenceOptions the token cache configuration options
     * @return An updated instance of this builder with the token cache options configured.
     */
    public DeviceCodeCredentialBuilder tokenCachePersistenceOptions(TokenCachePersistenceOptions
                                                                          tokenCachePersistenceOptions) {
        this.identityClientOptions.setTokenCacheOptions(tokenCachePersistenceOptions);
        return this;
    }

    /**
     * Sets the {@link AuthenticationRecord} captured from a previous authentication.
     *
     * @param authenticationRecord the authentication record to be configured.
     *
     * @return An updated instance of this builder with the configured authentication record.
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
        String clientId = this.clientId != null ? this.clientId : IdentityConstants.DEVELOPER_SINGLE_SIGN_ON_ID;
        return new DeviceCodeCredential(clientId, tenantId, challengeConsumer, automaticAuthentication,
                identityClientOptions);
    }
}
