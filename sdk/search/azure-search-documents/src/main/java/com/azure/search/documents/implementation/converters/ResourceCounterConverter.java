// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.ResourceCounter;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.ResourceCounter} and
 * {@link ResourceCounter}.
 */
public final class ResourceCounterConverter {
    private static final ClientLogger LOGGER = new ClientLogger(ResourceCounterConverter.class);

    /**
     * Maps from {@link com.azure.search.documents.implementation.models.ResourceCounter} to {@link ResourceCounter}.
     */
    public static ResourceCounter map(com.azure.search.documents.implementation.models.ResourceCounter obj) {
        if (obj == null) {
            return null;
        }
        ResourceCounter resourceCounter = new ResourceCounter();

        long _usage = obj.getUsage();
        resourceCounter.setUsage(_usage);

        Long _quota = obj.getQuota();
        resourceCounter.setQuota(_quota);
        return resourceCounter;
    }

    /**
     * Maps from {@link ResourceCounter} to {@link com.azure.search.documents.implementation.models.ResourceCounter}.
     */
    public static com.azure.search.documents.implementation.models.ResourceCounter map(ResourceCounter obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.ResourceCounter resourceCounter =
            new com.azure.search.documents.implementation.models.ResourceCounter();

        long _usage = obj.getUsage();
        resourceCounter.setUsage(_usage);

        Long _quota = obj.getQuota();
        resourceCounter.setQuota(_quota);
        return resourceCounter;
    }
}
