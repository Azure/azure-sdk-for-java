// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.implementation.util;

import com.azure.ai.metricsadvisor.models.AzureCosmosDataFeedSource;

public final class AzureCosmosDataFeedSourceAccessor {
    private static Accessor accessor;

    private AzureCosmosDataFeedSourceAccessor() {
    }

    /**
     * Type defining the methods to set the non-public properties of
     * an {@link AzureCosmosDataFeedSource} instance.
     */
    public interface Accessor {
        String getConnectionString(AzureCosmosDataFeedSource feedSource);
    }

    /**
     * The method called from {@link AzureCosmosDataFeedSource} to set it's accessor.
     *
     * @param accessor The accessor.
     */
    public static void setAccessor(final Accessor accessor) {
        AzureCosmosDataFeedSourceAccessor.accessor = accessor;
    }

    public static String getConnectionString(AzureCosmosDataFeedSource feedSource) {
        return accessor.getConnectionString(feedSource);
    }
}
