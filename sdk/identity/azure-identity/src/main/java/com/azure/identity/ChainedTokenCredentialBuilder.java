// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.credential.TokenCredential;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Fluent credential builder for instantiating a {@link ChainedTokenCredential}.
 *
 * @see ChainedTokenCredential
 */
public class ChainedTokenCredentialBuilder {
    private final Deque<TokenCredential> credentials;

    /**
     * Creates an instance of the builder to config the credential.
     */
    public ChainedTokenCredentialBuilder() {
        this.credentials = new ArrayDeque<>();
    }

    /**
     * Adds a credential to try to authenticate at the front of the chain.
     *
     * @param credential the credential to be added to the front of chain
     * @return the ChainedTokenCredential itself
     */
    public ChainedTokenCredentialBuilder addFirst(TokenCredential credential) {
        credentials.addFirst(credential);
        return this;
    }

    /**
     * Adds a credential to try to authenticate at the last of the chain.
     * @param credential the credential to be added to the end of chain
     * @return the ChainedTokenCredential itself
     */
    public ChainedTokenCredentialBuilder addLast(TokenCredential credential) {
        credentials.addLast(credential);
        return this;
    }

    /**
     * Creates a new {@link ChainedTokenCredential} with the current configurations.
     *
     * @return a {@link ChainedTokenCredential} with the current configurations.
     */
    public ChainedTokenCredential build() {
        return new ChainedTokenCredential(new ArrayDeque<>(credentials));
    }
}
