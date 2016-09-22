/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByName;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;

/**
 * Entry point to features management API.
 */
@Fluent
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
    @LangDefinition(ContainerName = "~/Feature")
    interface InResourceProvider extends
            SupportsListing<Feature>,
            SupportsGettingByName<Feature> {
        /**
         * Registers a feature in a resource provider.
         *
         * @param featureName the name of the feature
         * @return the immutable client-side feature object created
         */
        Feature register(String featureName);
    }
}
