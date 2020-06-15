// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.ResourceCounter;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.ResourceCounter} and
 * {@link ResourceCounter}.
 */
public final class ResourceCounterConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.ResourceCounter} to {@link ResourceCounter}.
     */
    public static ResourceCounter map(com.azure.search.documents.indexes.implementation.models.ResourceCounter obj) {
        if (obj == null) {
            return null;
        }
        ResourceCounter resourceCounter = new ResourceCounter();

        long usage = obj.getUsage();
        resourceCounter.setUsage(usage);

        Long quota = obj.getQuota();
        resourceCounter.setQuota(quota);
        return resourceCounter;
    }

    /**
     * Maps from {@link ResourceCounter} to {@link com.azure.search.documents.indexes.implementation.models.ResourceCounter}.
     */
    public static com.azure.search.documents.indexes.implementation.models.ResourceCounter map(ResourceCounter obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.indexes.implementation.models.ResourceCounter resourceCounter =
            new com.azure.search.documents.indexes.implementation.models.ResourceCounter();

        long usage = obj.getUsage();
        resourceCounter.setUsage(usage);

        Long quota = obj.getQuota();
        resourceCounter.setQuota(quota);
        return resourceCounter;
    }

    private ResourceCounterConverter() {
    }
}
