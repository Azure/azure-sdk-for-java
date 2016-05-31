/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByName;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;

import java.io.IOException;

/**
 * Entry point to features management API.
 */
public interface Features extends
        SupportsListing<Feature> {
    /**
     * Filter the features by a specific resource provider.
     *
     * @param resourceProviderName the name of the resource provider
     * @return an instance for accessing features in a resource provider
     */
    InResourceProvider resourceProvider(String resourceProviderName);

    /**
     * Entry point to features management API in a specific resource provider.
     */
    interface InResourceProvider extends
            SupportsListing<Feature>,
            SupportsGettingByName<Feature> {
        /**
         * Registers a feature in a resource provider.
         *
         * @param featureName the name of the feature
         * @return the immutable client-side feature object created
         * @throws IOException exception from serialization/deserialization
         * @throws CloudException exception from Azure
         */
        Feature register(String featureName) throws IOException, CloudException;
    }
}
