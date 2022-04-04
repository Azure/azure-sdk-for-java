// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.credential;

import com.azure.spring.cloud.core.properties.AzureProperties;

import java.util.Comparator;
import java.util.List;

/**
 * A {@link AzureCredentialResolvers} can resolve the Azure credential from the {@link AzureProperties} with a list of
 * provided {@link AzureCredentialResolver}.
 */
public final class AzureCredentialResolvers {

    private final List<AzureCredentialResolver<?>> resolvers;

    /**
     * Create a {@link AzureCredentialResolvers} with a list of {@link AzureCredentialResolver} objects.
     * @param resolvers The list of resolvers used to resolve credential.
     */
    public AzureCredentialResolvers(List<AzureCredentialResolver<?>> resolvers) {
        this.resolvers = resolvers;
    }

    /**
     * Create a {@link AzureCredentialResolvers} with a list of {@link AzureCredentialResolver} objects, and the order
     * of the resolvers will be determined by the comparator.
     * @param resolvers The list of resolvers used to resolve credential.
     * @param comparator The comparator to determine the execution order of the resolvers.
     */
    public AzureCredentialResolvers(List<AzureCredentialResolver<?>> resolvers, Comparator<AzureCredentialResolver<?>> comparator) {
        this(resolvers);
        this.resolvers.sort(comparator);
    }

    /**
     * Resolve the Azure credential from the {@link AzureProperties}.
     * @param azureProperties The {@link AzureProperties} object.
     * @return An azure credential object.
     */
    public Object resolve(AzureProperties azureProperties) {
        Object credential = null;
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
