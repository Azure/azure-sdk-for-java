// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.credential;

import com.azure.core.credentials.AccessToken;
import com.azure.identity.IdentityClient;
import com.azure.identity.IdentityClientOptions;
import reactor.core.publisher.Mono;

/**
 * An AAD credential that acquires a token with a username and a password. Users with 2FA/MFA (Multi-factored auth)
 * turned on will not be able to use this credential. Please use {@link DeviceCodeCredential} or {@link InteractiveBrowserCredential}
 * instead, or create a service principal if you want to authenticate silently.
 */
public class UserCredential extends AadCredential<UserCredential> {
    private final String username;
    private final String password;
    private final IdentityClient identityClient;

    /**
     * Creates a UserCredential with default identity client options.
     *
     * @param username the username of the user
     * @param password the password of the user
     */
    public UserCredential(String username, String password) {
        this(username, password, new IdentityClientOptions());
    }

    /**
     * Creates a UserCredential with the given identity client options.
     *
     * @param username the username of the user
     * @param password the password of the user
     * @param identityClientOptions the options for configuring the identity client
     */
    public UserCredential(String username, String password, IdentityClientOptions identityClientOptions) {
        this.username = username;
        this.password = password;
        identityClient = new IdentityClient(identityClientOptions);
    }

    @Override
    public Mono<AccessToken> getToken(String... scopes) {
        validate();
        return identityClient.authenticateWithUsernamePassword(tenantId(), clientId(), scopes, username, password);
    }
}
