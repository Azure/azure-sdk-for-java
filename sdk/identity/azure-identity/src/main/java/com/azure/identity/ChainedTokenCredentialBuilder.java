// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.credential.TokenCredential;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
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
     * Adds all of the credentials in the specified collection at the end
     * of this chain, as if by calling {@link ChainedTokenCredentialBuilder#addLast(TokenCredential)} on each one,
     * in the order that they are returned by the collection's iterator.
     *
     * @param credentials the collection of credentials to be appended to the chain.
     * @return An updated instance of the builder.
     */
    public ChainedTokenCredentialBuilder addAll(Collection<? extends TokenCredential> credentials) {
        this.credentials.addAll(credentials);
        return this;
    }

    /**
     * Creates a new {@link ChainedTokenCredential} with the current configurations.
     *
     * @return a {@link ChainedTokenCredential} with the current configurations.
     */
    public ChainedTokenCredential build() {
        return new ChainedTokenCredential(new ArrayList<TokenCredential>(credentials));
    }
}
