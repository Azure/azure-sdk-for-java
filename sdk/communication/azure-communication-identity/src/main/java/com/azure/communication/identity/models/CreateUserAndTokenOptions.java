// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.identity.models;

import com.azure.core.annotation.Immutable;

import java.time.Duration;

@Immutable
public final class CreateUserAndTokenOptions {
    private Iterable<CommunicationTokenScope> scopes;
    private Duration expiresInMinutes;

    /**
     * Constructor of {@link CreateUserAndTokenOptions}.
     * @param scopes List of {@link CommunicationTokenScope} scopes for the Communication Identity access token.
     */
    public CreateUserAndTokenOptions(Iterable<CommunicationTokenScope> scopes){
        this.scopes = scopes;
    }

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
    public CreateUserAndTokenOptions setExpiresInMinutes(Duration expiresInMinutes){
        this.expiresInMinutes = expiresInMinutes;
        return this;
    }
}
