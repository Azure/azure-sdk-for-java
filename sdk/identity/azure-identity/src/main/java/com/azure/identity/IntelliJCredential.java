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

/**
 * A credential provider that provides token credentials from Azure Tools for IntelliJ plugin credential cache.
 *
 * <p> If the developer has authenticated successfully with Azure Tools for IntelliJ plugin in the IntelliJ IDE then
 * this credential can be used in the development code to reuse the cached plugin credentials.</p>
 */
@Immutable
class IntelliJCredential implements TokenCredential {
    private final IdentityClient identityClient;
    private final AtomicReference<MsalToken> cachedToken;

    /**
     * Creates an {@link IntelliJCredential} with default identity client options.
     * @param identityClientOptions the options to configure the identity client
     */
    IntelliJCredential(IdentityClientOptions identityClientOptions) {
        identityClient = new IdentityClientBuilder().identityClientOptions(identityClientOptions)
                             .clientId("61d65f5a-6e3b-468b-af73-a033f5098c5c").build();

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
            Mono.defer(() -> identityClient.authenticateWithIntelliJ(request)))
                   .map(msalToken -> {
                       cachedToken.set(msalToken);
                       return msalToken;
                   });
    }
}
