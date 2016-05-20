package com.microsoft.azure.management.resources;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsGetting;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;

import java.io.IOException;

/**
 * Defines an interface for accessing features in Azure.
 */
public interface Features extends
        SupportsListing<Feature> {
    /**
     * Filter the features by a specific resource provider.
     *
     * @param resourceProviderName the name of the resource provider.
     * @return an instance for accessing features in a resource provider.
     */
    InResourceProvider resourceProvider(String resourceProviderName);

    /**
     * Defines an interface for accessing features in a resource provider.
     */
    interface InResourceProvider extends
            SupportsListing<Feature>,
            SupportsGetting<Feature> {
        Feature register(String featureName) throws IOException, CloudException;
    }
}
