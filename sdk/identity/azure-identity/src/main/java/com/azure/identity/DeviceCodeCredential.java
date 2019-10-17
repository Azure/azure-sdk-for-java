// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.annotation.Immutable;
import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
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
    private final Consumer<DeviceCodeInfo> challengeConsumer;
    private final IdentityClient identityClient;
    private final AtomicReference<MsalToken> cachedToken;

    /**
     * Creates a DeviceCodeCredential with the given identity client options.
     *
     * @param clientId the client ID of the application
     * @param tenantId the tenant ID of the application
     * @param challengeConsumer a method allowing the user to meet the device code challenge
     * @param identityClientOptions the options for configuring the identity client
     */
    DeviceCodeCredential(String clientId, String tenantId, Consumer<DeviceCodeInfo> challengeConsumer,
                         IdentityClientOptions identityClientOptions) {
        this.challengeConsumer = challengeConsumer;
        if (tenantId == null) {
            tenantId = "common";
        }
        identityClient = new IdentityClientBuilder()
            .tenantId(tenantId)
            .clientId(clientId)
            .identityClientOptions(identityClientOptions)
            .build();
        this.cachedToken = new AtomicReference<>();
    }

    @Override
    public Mono<AccessToken> getToken(TokenRequestContext request) {
        return Mono.defer(() -> {
            if (cachedToken.get() != null) {
                return identityClient.authenticateWithUserRefreshToken(request, cachedToken.get())
                    .onErrorResume(t -> Mono.empty());
            } else {
                return Mono.empty();
            }
        }).switchIfEmpty(
            Mono.defer(() -> identityClient.authenticateWithDeviceCode(request, challengeConsumer)))
            .map(msalToken -> {
                cachedToken.set(msalToken);
                return msalToken;
            });
    }
}
