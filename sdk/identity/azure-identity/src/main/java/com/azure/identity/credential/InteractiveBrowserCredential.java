// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.credential;

import com.azure.core.credentials.AccessToken;
import com.azure.identity.IdentityClient;
import com.azure.identity.IdentityClientOptions;
import com.microsoft.aad.msal4j.IAccount;
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
public class InteractiveBrowserCredential extends AadCredential<InteractiveBrowserCredential> {
    private int port;
    private final IdentityClient identityClient;

    /**
     * Creates a InteractiveBrowserCredential with a listening port, for which {@code http://localhost:{port}} must be
     * registered as a valid reply URL on the application.
     *
     * @param port the port on which the credential will listen for the browser authentication result
     */
    public InteractiveBrowserCredential(int port) {
        this(port, new IdentityClientOptions());
    }

    /**
     * Creates a InteractiveBrowserCredential with the given identity client options and a listening port, for which
     * {@code http://localhost:{port}} must be registered as a valid reply URL on the application.
     *
     * @param port the port on which the credential will listen for the browser authentication result
     * @param identityClientOptions the options for configuring the identity client
     */
    public InteractiveBrowserCredential(int port, IdentityClientOptions identityClientOptions) {
        this.port = port;
        identityClient = new IdentityClient(identityClientOptions);
    }

    @Override
    public Mono<AccessToken> getToken(String... scopes) {
        if (clientId() == null) {
            throw new IllegalArgumentException("Must provide non-null value for client id in " + this.getClass().getSimpleName());
        }
        return identityClient.authenticateWithCurrentlyLoggedInAccount(scopes).onErrorResume(t -> Mono.empty())
            .switchIfEmpty(identityClient.authenticateWithBrowserInteraction(clientId(), scopes, port));
    }
}
