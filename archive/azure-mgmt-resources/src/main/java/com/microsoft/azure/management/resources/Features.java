/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import rx.Observable;

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
    Observable<Feature> registerAsync(String resourceProviderName, String featureName);

    /**
     * Registers a feature in a resource provider asynchronously.
     *
     * @param resourceProviderName the name of the resource provider
     * @param featureName the name of the feature
     * @param callback the callback to call on success or failure
     * @return a handle to cancel the request
     */
    ServiceFuture<Feature> registerAsync(String resourceProviderName, String featureName, ServiceCallback<Feature> callback);
}
