// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.implementation.credential.descriptor;

import com.azure.core.credential.TokenCredential;
import com.azure.spring.cloud.core.credential.AzureCredentialResolver;

import java.util.function.Consumer;

/**
 * A descriptor describes the token authentication.
 */
public final class TokenAuthenticationDescriptor implements AuthenticationDescriptor<TokenCredential> {

    private final Consumer<TokenCredential> consumer;
    private final AzureCredentialResolver<TokenCredential> resolver;

    /**
     * Create a {@link TokenAuthenticationDescriptor} instance with the consumer to consume the resolved credential.
     * @param resolver A resolver to resolve token credential from properties.
     * @param consumer The consumer to consume the resolved credential.
     */
    public TokenAuthenticationDescriptor(AzureCredentialResolver<TokenCredential> resolver,
                                         Consumer<TokenCredential> consumer) {
        this.consumer = consumer;
        this.resolver = resolver;
    }

    @Override
    public Class<TokenCredential> getAzureCredentialType() {
        return TokenCredential.class;
    }

    @Override
    public AzureCredentialResolver<TokenCredential> getAzureCredentialResolver() {
        return this.resolver;
    }

    @Override
    public Consumer<TokenCredential> getConsumer() {
        return consumer;
    }
}
