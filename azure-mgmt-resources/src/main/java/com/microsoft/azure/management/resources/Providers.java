/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.apigeneration.Method;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByName;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
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
     * Unregisters provider from a subscription asynchronously.
     *
     * @param resourceProviderNamespace Namespace of the resource provider
     * @return a representation of the deferred computation of this call returning the unregistered Provider if successful
     */
    Observable<Provider> unregisterAsync(String resourceProviderNamespace);

    /**
     * Unregisters provider from a subscription asynchronously.
     *
     * @param resourceProviderNamespace namespace of the resource provider
     * @param callback the callback to call on success or failure with the ProviderInner object wrapped as parameter if successful
     * @return a handle to cancel the request
     */
    @Method
    ServiceFuture<Provider> unregisterAsync(String resourceProviderNamespace, ServiceCallback<Provider> callback);

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
    Observable<Provider> registerAsync(String resourceProviderNamespace);

    /**
     * Registers provider to be used with a subscription asynchronously.
     *
     * @param resourceProviderNamespace Namespace of the resource provider
     * @param callback the callback to call on success or failure with the ProviderInner object wrapped as parameter if successful
     * @return a handle to cancel the request
     */
    ServiceFuture<Provider> registerAsync(String resourceProviderNamespace, ServiceCallback<Provider> callback);

    /**
     * Gets the information about a provider from Azure based on the provider name.
     *
     * @param name the name of the provider
     * @return a representation of the deferred computation of this call returning the found provider, if any
     */
    Observable<Provider> getByNameAsync(String name);
}
