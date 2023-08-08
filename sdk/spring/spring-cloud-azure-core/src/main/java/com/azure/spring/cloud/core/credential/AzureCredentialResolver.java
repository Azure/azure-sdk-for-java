// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.credential;

import com.azure.spring.cloud.core.properties.AzureProperties;

/**
 * Resolver interface to resolve an azure credential implementation,
 * the method {@link AzureCredentialResolver#resolve(AzureProperties)}
 * returns null if the related properties does not match.
 *
 * @param <T> Azure credential implementation
 */
public interface AzureCredentialResolver<T> {

    /**
     * Resolve the credential according to the azure properties.
     * @param properties the azure properties.
     * @return the azure credential.
     */
    T resolve(AzureProperties properties);

    /**
     * Is the azure properties resolvable.
     * @param properties the azure properties.
     * @return true or false.
     */
    boolean isResolvable(AzureProperties properties);

}
