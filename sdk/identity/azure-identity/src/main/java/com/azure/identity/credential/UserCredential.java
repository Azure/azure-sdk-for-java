// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.credential;

import com.azure.core.credentials.AccessToken;
import com.azure.core.credentials.TokenCredential;
import com.azure.core.implementation.annotation.Immutable;
import com.azure.identity.implementation.IdentityClient;
import com.azure.identity.implementation.IdentityClientOptions;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * An AAD credential that acquires a token with a username and a password. Users with 2FA/MFA (Multi-factored auth)
 * turned on will not be able to use this credential. Please use {@link DeviceCodeCredential} or {@link InteractiveBrowserCredential}
 * instead, or create a service principal if you want to authenticate silently.
 */
@Immutable
public class UserCredential implements TokenCredential {
    private final String username;
    private final String password;
    private final IdentityClient identityClient;

    /**
     * Creates a UserCredential with the given identity client options.
     *
     * @param tenantId the tenant ID of the application
     * @param clientId the client ID of the application
     * @param username the username of the user
     * @param password the password of the user
     * @param identityClientOptions the options for configuring the identity client
     */
    UserCredential(String tenantId, String clientId, String username, String password, IdentityClientOptions identityClientOptions) {
        Objects.requireNonNull(username);
        Objects.requireNonNull(password);
        this.username = username;
        this.password = password;
        identityClient = new IdentityClient(tenantId, clientId, identityClientOptions);
    }

    @Override
    public Mono<AccessToken> getToken(String... scopes) {
        return identityClient.authenticateWithUsernamePassword(scopes, username, password);
    }
}
