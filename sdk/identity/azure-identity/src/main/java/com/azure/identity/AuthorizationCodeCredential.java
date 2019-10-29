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

import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;

/**
 * An AAD credential that acquires a token with an Oauth 2.0 authorization code grant
 * for an AAD application.
 */
@Immutable
public class AuthorizationCodeCredential implements TokenCredential {
    private final String authCode;
    private final URI redirectUri;
    private final IdentityClient identityClient;
    private final AtomicReference<MsalToken> cachedToken;

    /**
     * Creates an AuthorizationCodeCredential with the given identity client options.
     *
     * @param clientId the client ID of the application
     * @param tenantId the tenant ID of the application
     * @param authCode the Oauth 2.0 authorization code grant
     * @param redirectUri the redirect URI used to authenticate to Azure Active Directory
     * @param identityClientOptions the options for configuring the identity client
     */
    AuthorizationCodeCredential(String clientId, String tenantId, String authCode, URI redirectUri,
                                IdentityClientOptions identityClientOptions) {
        if (tenantId == null) {
            tenantId = "common";
        }
        identityClient = new IdentityClientBuilder()
            .tenantId(tenantId)
            .clientId(clientId)
            .identityClientOptions(identityClientOptions)
            .build();
        this.cachedToken = new AtomicReference<>();
        this.authCode = authCode;
        this.redirectUri = redirectUri;
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
            Mono.defer(() -> identityClient.authenticateWithAuthorizationCode(request, authCode, redirectUri)))
            .map(msalToken -> {
                cachedToken.set(msalToken);
                return msalToken;
            });
    }
}
