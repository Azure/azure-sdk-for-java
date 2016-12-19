/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByName;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;
import com.microsoft.rest.ServiceResponse;
import rx.Observable;

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
     * @return the ProviderInner object wrapped in {@link ServiceResponse} if successful
     */
    Provider unregister(String resourceProviderNamespace);

    /**
     * Registers provider to be used with a subscription.
     *
     * @param resourceProviderNamespace Namespace of the resource provider
     * @return the ProviderInner object wrapped in {@link ServiceResponse} if successful
     */
    Provider register(String resourceProviderNamespace);

    /**
     * Gets the information about a provider from Azure based on the provider name.
     *
     * @param name the name of the provider
     * @return an observable of the immutable representation of the Provider
     */
    Observable<Provider> getByNameAsync(String name);
}
