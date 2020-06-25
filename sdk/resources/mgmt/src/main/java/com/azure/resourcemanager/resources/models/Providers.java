// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingByName;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsListing;
import reactor.core.publisher.Mono;

/**
 * Entry point to providers management API.
 */
@Fluent
public interface Providers extends
        SupportsListing<Provider>,
        SupportsGettingByName<Provider> {
    /**
     * Unregisters provider from a subscription.
     *
     * @param resourceProviderNamespace Namespace of the resource provider
     * @return the Provider if successful
     */
    Provider unregister(String resourceProviderNamespace);

    /**
     * Unregisters provider from a subscription asynchronously.
     *
     * @param resourceProviderNamespace Namespace of the resource provider
     * @return a representation of the deferred computation of this call
     *         returning the unregistered Provider if successful
     */
    Mono<Provider> unregisterAsync(String resourceProviderNamespace);

    /**
     * Registers provider to be used with a subscription.
     *
     * @param resourceProviderNamespace namespace of the resource provider
     * @return the registered provider
     */
    Provider register(String resourceProviderNamespace);

    /**
     * Registers provider to be used with a subscription asynchronously.
     *
     * @param resourceProviderNamespace Namespace of the resource provider
     * @return a representation of the deferred computation of this call returning the registered provider if successful
     */
    Mono<Provider> registerAsync(String resourceProviderNamespace);

    /**
     * Gets the information about a provider from Azure based on the provider name.
     *
     * @param name the name of the provider
     * @return a representation of the deferred computation of this call returning the found provider, if any
     */
    Mono<Provider> getByNameAsync(String name);
}
