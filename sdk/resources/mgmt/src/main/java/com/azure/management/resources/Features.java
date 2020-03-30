/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.resources;

import com.azure.core.annotation.Fluent;
import com.azure.management.resources.fluentcore.collection.SupportsListing;
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
     * @param featureName          the name of the feature
     * @return the registered feature
     */
    Feature register(String resourceProviderName, String featureName);

    /**
     * Registers a feature in a resource provider asynchronously.
     *
     * @param resourceProviderName the name of the resource provider
     * @param featureName          the name of the feature
     * @return a representation of the deferred computation of this call returning the registered feature
     */
    Mono<Feature> registerAsync(String resourceProviderName, String featureName);
}
