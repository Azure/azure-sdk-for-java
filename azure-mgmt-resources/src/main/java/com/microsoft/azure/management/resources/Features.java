/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;

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
     * @return the immutable client-side feature object created
     */
    Feature register(String resourceProviderName, String featureName);
}
