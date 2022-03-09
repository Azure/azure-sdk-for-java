// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.implementation.util;

import com.azure.ai.metricsadvisor.administration.models.AzureCosmosDbDataFeedSource;

public final class AzureCosmosDbDataFeedSourceAccessor {
    private static Accessor accessor;

    private AzureCosmosDbDataFeedSourceAccessor() {
    }

    /**
     * Type defining the methods to set the non-public properties of
     * an {@link AzureCosmosDbDataFeedSource} instance.
     */
    public interface Accessor {
        String getConnectionString(AzureCosmosDbDataFeedSource feedSource);
    }

    /**
     * The method called from {@link AzureCosmosDbDataFeedSource} to set it's accessor.
     *
     * @param accessor The accessor.
     */
    public static void setAccessor(final Accessor accessor) {
        AzureCosmosDbDataFeedSourceAccessor.accessor = accessor;
    }

    public static String getConnectionString(AzureCosmosDbDataFeedSource feedSource) {
        return accessor.getConnectionString(feedSource);
    }
}
