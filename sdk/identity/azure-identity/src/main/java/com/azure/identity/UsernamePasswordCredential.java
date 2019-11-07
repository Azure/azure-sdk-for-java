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

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * An AAD credential that acquires a token with a username and a password. Users with 2FA/MFA (Multi-factored auth)
 * turned on will not be able to use this credential. Please use {@link DeviceCodeCredential} or {@link
 * InteractiveBrowserCredential} instead, or create a service principal if you want to authenticate silently.
 */
@Immutable
public class UsernamePasswordCredential implements TokenCredential {
    private final String username;
    private final String password;
    private final IdentityClient identityClient;
    private final AtomicReference<MsalToken> cachedToken;

    /**
     * Creates a UserCredential with the given identity client options.
     *
     * @param clientId the client ID of the application
     * @param tenantId the tenant ID of the application
     * @param username the username of the user
     * @param password the password of the user
     * @param identityClientOptions the options for configuring the identity client
     */
    UsernamePasswordCredential(String clientId, String tenantId, String username, String password,
                               IdentityClientOptions identityClientOptions) {
        Objects.requireNonNull(username, "'username' cannot be null.");
        Objects.requireNonNull(password, "'password' cannot be null.");
        this.username = username;
        this.password = password;
        if (tenantId == null) {
            tenantId = "common";
        }
        identityClient =
            new IdentityClientBuilder()
                .tenantId(tenantId)
                .clientId(clientId)
                .identityClientOptions(identityClientOptions)
                .build();
        cachedToken = new AtomicReference<>();
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
        }).switchIfEmpty(Mono.defer(() -> identityClient.authenticateWithUsernamePassword(request, username, password)))
            .map(msalToken -> {
                cachedToken.set(msalToken);
                return msalToken;
            });
    }
}
