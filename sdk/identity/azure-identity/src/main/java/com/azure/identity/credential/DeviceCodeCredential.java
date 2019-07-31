// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.credential;

import com.azure.core.credentials.AccessToken;
import com.azure.core.credentials.TokenCredential;
import com.azure.core.implementation.annotation.Immutable;
import com.azure.identity.DeviceCodeChallenge;
import com.azure.identity.implementation.IdentityClient;
import com.azure.identity.implementation.IdentityClientBuilder;
import com.azure.identity.implementation.IdentityClientOptions;
import com.azure.identity.implementation.MsalToken;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * An AAD credential that acquires a token with a device code for an AAD application.
 */
@Immutable
public class DeviceCodeCredential implements TokenCredential {
    private final Consumer<DeviceCodeChallenge> deviceCodeChallengeConsumer;
    private final IdentityClient identityClient;
    private final AtomicReference<MsalToken> cachedToken;

    /**
     * Creates a DeviceCodeCredential with the given identity client options.
     *
     * @param clientId the client ID of the application
     * @param deviceCodeChallengeConsumer a method allowing the user to meet the device code challenge
     * @param identityClientOptions the options for configuring the identity client
     */
    DeviceCodeCredential(String clientId, Consumer<DeviceCodeChallenge> deviceCodeChallengeConsumer, IdentityClientOptions identityClientOptions) {
        this.deviceCodeChallengeConsumer = deviceCodeChallengeConsumer;
        identityClient = new IdentityClientBuilder().tenantId("common").clientId(clientId).identityClientOptions(identityClientOptions).build();
        this.cachedToken = new AtomicReference<>();
    }

    @Override
    public Mono<AccessToken> getToken(String... scopes) {
        return Mono.defer(() -> {
            if (cachedToken.get() != null) {
                return identityClient.authenticateWithUserRefreshToken(scopes, cachedToken.get()).onErrorResume(t -> Mono.empty());
            } else {
                return Mono.empty();
            }
        }).switchIfEmpty(Mono.defer(() -> identityClient.authenticateWithDeviceCode(scopes, deviceCodeChallengeConsumer)))
            .map(msalToken -> {
                cachedToken.set(msalToken);
                return msalToken;
            });
    }
}
