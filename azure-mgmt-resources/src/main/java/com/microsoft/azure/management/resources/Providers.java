package com.microsoft.azure.management.resources;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsGetting;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;
import com.microsoft.rest.ServiceResponse;

import java.io.IOException;

/**
 * Defines an interface for accessing information about resource providers in Azure.
 */
public interface Providers extends
        SupportsListing<Provider>,
        SupportsGetting<Provider> {
    /**
     * Unregisters provider from a subscription.
     *
     * @param resourceProviderNamespace Namespace of the resource provider.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @return the ProviderInner object wrapped in {@link ServiceResponse} if successful.
     */
    Provider unregister(String resourceProviderNamespace) throws CloudException, IOException;

    /**
     * Registers provider to be used with a subscription.
     *
     * @param resourceProviderNamespace Namespace of the resource provider.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @return the ProviderInner object wrapped in {@link ServiceResponse} if successful.
     */
    Provider register(String resourceProviderNamespace) throws CloudException, IOException;
}
