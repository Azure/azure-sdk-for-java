// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsListing;
import reactor.core.publisher.Mono;

/**
 * Entry point to features management API.
 */
@Fluent
public interface Features extends
        SupportsListing<Feature> {
    /**
     * Registers a feature in a resource provider.
     *
     * @param resourceProviderName the name of the resource provider
     * @param featureName the name of the feature
     * @return the registered feature
     */
    Feature register(String resourceProviderName, String featureName);

    /**
     * Registers a feature in a resource provider asynchronously.
     *
     * @param resourceProviderName the name of the resource provider
     * @param featureName the name of the feature
     * @return a representation of the deferred computation of this call returning the registered feature
     */
    Mono<Feature> registerAsync(String resourceProviderName, String featureName);
}
