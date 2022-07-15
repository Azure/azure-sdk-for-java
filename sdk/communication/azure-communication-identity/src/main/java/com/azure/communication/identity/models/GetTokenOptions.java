// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.identity.models;

import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.communication.identity.CommunicationIdentityAsyncClient;
import com.azure.communication.identity.CommunicationIdentityClient;
import com.azure.core.annotation.Immutable;

import java.time.Duration;

/**
 * Options class for configuring the {@link
 * CommunicationIdentityAsyncClient#getToken(GetTokenOptions)} and {@link
 * CommunicationIdentityClient#getToken(GetTokenOptions)} methods.
 */
@Immutable
public final class GetTokenOptions {

    private CommunicationUserIdentifier communicationUser;
    private Iterable<CommunicationTokenScope> scopes;
    private Duration expiresInMinutes;

    /**
     * Constructor of {@link GetTokenOptions}.
     *
     * @param communicationUser The user to be issued tokens.
     * @param scopes The scopes that the token should have.
     */
    public GetTokenOptions(CommunicationUserIdentifier communicationUser, Iterable<CommunicationTokenScope> scopes){
        this.communicationUser = communicationUser;
        this.scopes = scopes;
    }

    /**
     * Gets the communication user.
     *
     * @return the communication user.
     */
    public CommunicationUserIdentifier getCommunicationUser() { return this.communicationUser; }

    /**
     * Gets the scopes that the token should have.
     *
     * @return the scopes that the token should have.
     */
    public Iterable<CommunicationTokenScope> getScopes() { return this.scopes; }

    /**
     * Gets token expiration time.
     *
     * @return token expiration time.
     */
    public Duration getExpiresInMinutes() { return this.expiresInMinutes; }

    /**
     * Sets token expiration time. Valid period of the token should be within [60,1440] minutes range.
     *
     * @return {@link GetTokenOptions}.
     */
    public GetTokenOptions setExpiresInMinutes(Duration expiresInMinutes){
        this.expiresInMinutes = expiresInMinutes;
        return this;
    }
}
