// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.credential;

import com.azure.core.credentials.AccessToken;
import com.azure.identity.DeviceCodeChallenge;
import com.azure.identity.IdentityClient;
import com.azure.identity.IdentityClientOptions;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

/**
 * An AAD credential that acquires a token with a device code for an AAD application.
 */
public class DeviceCodeCredential extends AadCredential<DeviceCodeCredential> {
    private final Consumer<DeviceCodeChallenge> deviceCodeChallengeConsumer;
    private final IdentityClient identityClient;

    /**
     * Creates a DeviceCodeCredential with default identity client options.
     *
     * @param deviceCodeChallengeConsumer a method allowing the user to meet the device code challenge
     */
    public DeviceCodeCredential(Consumer<DeviceCodeChallenge> deviceCodeChallengeConsumer) {
        this(deviceCodeChallengeConsumer, new IdentityClientOptions());
    }

    /**
     * Creates a DeviceCodeCredential with the given identity client options.
     *
     * @param deviceCodeChallengeConsumer a method allowing the user to meet the device code challenge
     * @param identityClientOptions the options for configuring the identity client
     */
    public DeviceCodeCredential(Consumer<DeviceCodeChallenge> deviceCodeChallengeConsumer, IdentityClientOptions identityClientOptions) {
        this.deviceCodeChallengeConsumer = deviceCodeChallengeConsumer;
        identityClient = new IdentityClient(identityClientOptions);
    }

    @Override
    public Mono<AccessToken> getToken(String... scopes) {
        if (clientId() == null) {
            throw new IllegalArgumentException("Must provide non-null value for client id in " + this.getClass().getSimpleName());
        }
        return Mono.defer(() -> identityClient.authenticateWithCurrentlyLoggedInAccount(scopes).onErrorResume(t -> Mono.empty()))
            .switchIfEmpty(Mono.defer(() -> identityClient.authenticateWithDeviceCode(clientId(), scopes, deviceCodeChallengeConsumer)));
    }
}
