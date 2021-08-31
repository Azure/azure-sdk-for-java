// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.credential.resolver;

import com.azure.spring.core.credential.provider.AzureCredentialProvider;
import com.azure.spring.core.properties.AzureProperties;

import java.util.Comparator;
import java.util.List;

/**
 * Resolve the Azure credential resolver.
 */
public class AzureCredentialResolvers {

    private final List<AzureCredentialResolver<?>> resolvers;

    public AzureCredentialResolvers(List<AzureCredentialResolver<?>> resolvers) {
        this.resolvers = resolvers;
    }

    public AzureCredentialResolvers(List<AzureCredentialResolver<?>> resolvers, Comparator<AzureCredentialResolver<?>> comparator) {
        this(resolvers);
        this.resolvers.sort(comparator);
    }

    public AzureCredentialProvider<?> resolve(AzureProperties azureProperties) {
        AzureCredentialProvider<?> credential = null;
        for (AzureCredentialResolver<?> resolver : this.resolvers) {
            if (!resolver.isResolvable(azureProperties)) {
                continue;
            }

            credential = resolver.resolve(azureProperties);
            if (credential != null) {
                break;
            }
        }
        return credential;
    }
}
