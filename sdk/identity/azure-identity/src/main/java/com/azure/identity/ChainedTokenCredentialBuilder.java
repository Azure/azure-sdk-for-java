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
 * The {@link ChainedTokenCredential} is a convenience credential that allows users to chain together a set of
 * TokenCredential together. The credential executes each credential in the chain sequentially and returns the token
 * from the first credential in the chain that successfully authenticates.
 *
 * <p><strong>Sample: Construct a ChainedTokenCredential.</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a {@link com.azure.identity.ChainedTokenCredential},
 * using the {@link com.azure.identity.ChainedTokenCredentialBuilder} to configure it. The sample below
 * tries silent username+password login tried first, then interactive browser login as needed
 * (e.g. when 2FA is turned on in the directory). Once this credential is created, it may be passed into the builder
 * of many of the Azure SDK for Java client builders as the 'credential' parameter.</p>
 *
 * <!-- src_embed com.azure.identity.credential.chainedtokencredential.construct -->
 * <pre>
 * TokenCredential usernamePasswordCredential = new UsernamePasswordCredentialBuilder&#40;&#41;
 *     .clientId&#40;clientId&#41;
 *     .username&#40;fakeUsernamePlaceholder&#41;
 *     .password&#40;fakePasswordPlaceholder&#41;
 *     .build&#40;&#41;;
 * TokenCredential interactiveBrowserCredential = new InteractiveBrowserCredentialBuilder&#40;&#41;
 *     .clientId&#40;clientId&#41;
 *     .port&#40;8765&#41;
 *     .build&#40;&#41;;
 * TokenCredential credential = new ChainedTokenCredentialBuilder&#40;&#41;
 *     .addLast&#40;usernamePasswordCredential&#41;
 *     .addLast&#40;interactiveBrowserCredential&#41;
 *     .build&#40;&#41;;
 * </pre>
 * <!-- end com.azure.identity.credential.chainedtokencredential.construct -->
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
