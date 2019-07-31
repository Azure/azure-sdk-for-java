// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.credential;

import com.azure.core.credentials.AccessToken;
import com.azure.core.credentials.TokenCredential;
import com.azure.core.implementation.annotation.Immutable;
import com.azure.identity.implementation.IdentityClient;
import com.azure.identity.implementation.IdentityClientBuilder;
import com.azure.identity.implementation.IdentityClientOptions;
import com.azure.identity.implementation.MsalToken;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicReference;

/**
 * An AAD credential that acquires a token for an AAD application by prompting the login in the default browser. When
 * authenticated, the oauth2 flow will notify the credential of the authentication code through the reply URL.
 *
 * <p>
 * The application to authenticate to must have delegated user login permissions and have {@code http://localhost:{port}}
 * listed as a valid reply URL.
 */
@Immutable
public class InteractiveBrowserCredential implements TokenCredential {
    private final int port;
    private final IdentityClient identityClient;
    private final AtomicReference<MsalToken> cachedToken;

    /**
     * Creates a InteractiveBrowserCredential with the given identity client options and a listening port, for which
     * {@code http://localhost:{port}} must be registered as a valid reply URL on the application.
     *
     * @param clientId the client ID of the application
     * @param port the port on which the credential will listen for the browser authentication result
     * @param identityClientOptions the options for configuring the identity client
     */
    InteractiveBrowserCredential(String clientId, int port, IdentityClientOptions identityClientOptions) {
        this.port = port;
        identityClient = new IdentityClientBuilder().tenantId("common").clientId(clientId).identityClientOptions(identityClientOptions).build();
        cachedToken = new AtomicReference<>();
    }

    @Override
    public Mono<AccessToken> getToken(String... scopes) {
        return Mono.defer(() -> {
            if (cachedToken.get() != null) {
                return identityClient.authenticateWithUserRefreshToken(scopes, cachedToken.get()).onErrorResume(t -> Mono.empty());
            } else {
                return Mono.empty();
            }
        }).switchIfEmpty(Mono.defer(() -> identityClient.authenticateWithBrowserInteraction(scopes, port)))
            .map(msalToken -> {
                cachedToken.set(msalToken);
                return msalToken;
            });
    }
}
