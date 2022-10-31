// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.implementation.credential.resolver;

import com.azure.core.credential.TokenCredential;
import com.azure.spring.cloud.core.credential.AzureCredentialResolver;
import com.azure.spring.cloud.core.properties.AzureProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Resolve the token credential according to the azure properties.
 */
public class AzureTokenCredentialResolver implements AzureCredentialResolver<TokenCredential> {

    private final List<AzureCredentialResolver<TokenCredential>> resolvers;

    public AzureTokenCredentialResolver(List<AzureCredentialResolver<TokenCredential>> resolvers) {
        this.resolvers = resolvers;
    }

    public AzureTokenCredentialResolver() {
        this.resolvers = new ArrayList<>();
        this.resolvers.add(new ClientSecretCredentialResolver());
        this.resolvers.add(new ClientCertificateCredentialResolver());
        this.resolvers.add(new UsernamePasswordCredentialResolver());
        this.resolvers.add(new ManagedIdentityCredentialResolver());
        this.resolvers.add(new DefaultAzureCredentialResolver());
    }

    @Override
    public TokenCredential resolve(AzureProperties properties) {
        for (AzureCredentialResolver<TokenCredential> resolver : resolvers) {
            if (resolver.isResolvable(properties)) {
                return resolver.resolve(properties);
            }
        }
        return null;
    }

    @Override
    public boolean isResolvable(AzureProperties properties) {
        return true;
    }

}
