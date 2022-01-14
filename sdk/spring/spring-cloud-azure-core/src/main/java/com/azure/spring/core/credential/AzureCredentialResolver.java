// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.credential;

import com.azure.spring.core.credential.provider.AzureCredentialProvider;
import com.azure.spring.core.properties.AzureProperties;

/**
 * Resolver interface to resolve an azure credential provider implementation,
 * the method {@link AzureCredentialResolver#resolve(AzureProperties)}
 * returns null if the related properties does not match.
 *
 * @param <T> Azure credential implementation
 */
public interface AzureCredentialResolver<T extends AzureCredentialProvider<?>> {

    /**
     * Resolve the credential provider according to the azure properties.
     * @param properties the azure properties.
     * @return the azure credential provider.
     */
    T resolve(AzureProperties properties);

    /**
     * Is the azure properties resolvable.
     * @param properties the azure properties.
     * @return true or false.
     */
    boolean isResolvable(AzureProperties properties);

}
