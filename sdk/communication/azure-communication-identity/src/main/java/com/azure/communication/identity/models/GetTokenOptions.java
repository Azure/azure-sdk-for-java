// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.identity.models;

import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.communication.identity.CommunicationIdentityAsyncClient;
import com.azure.communication.identity.CommunicationIdentityClient;
import com.azure.core.annotation.Immutable;

import java.time.Duration;

/**
 * Options used to get a Communication Identity access token for a {@link CommunicationUserIdentifier}.
 */
@Immutable
public final class GetTokenOptions {

    private CommunicationUserIdentifier communicationUser;
    private Iterable<CommunicationTokenScope> scopes;
    private Duration expiresInMinutes;

    /**
     * Constructor of {@link GetTokenOptions}.
     *
     * @param communicationUser The {@link CommunicationUserIdentifier} for whom to get a Communication Identity access
     * token.
     * @param scopes List of {@link CommunicationTokenScope} scopes for the Communication Identity access token.
     */
    public GetTokenOptions(CommunicationUserIdentifier communicationUser, Iterable<CommunicationTokenScope> scopes){
        this.communicationUser = communicationUser;
        this.scopes = scopes;
    }

    /**
     * Gets the {@link CommunicationUserIdentifier}.
     *
     * @return the {@link CommunicationUserIdentifier}.
     */
    public CommunicationUserIdentifier getCommunicationUser() { return this.communicationUser; }

    /**
     * Gets the scopes for the Communication Identity access token.
     *
     * @return the scopes for the Communication Identity access token.
     */
    public Iterable<CommunicationTokenScope> getScopes() { return this.scopes; }

    /**
     * Gets the Communication Identity access token expiration time.
     *
     * @return the Communication Identity access token expiration time.
     */
    public Duration getExpiresInMinutes() { return this.expiresInMinutes; }

    /**
     * Sets the Communication Identity access token expiration time. Valid period of the token should be within
     * &lt;60,1440&gt; minutes range.
     *
     * @param expiresInMinutes Communication Identity access token expiration time. Valid period of the token should be
     * within &lt;60,1440&gt; minutes range.
     * @return {@link GetTokenOptions}.
     */
    public GetTokenOptions setExpiresInMinutes(Duration expiresInMinutes){
        this.expiresInMinutes = expiresInMinutes;
        return this;
    }
}
