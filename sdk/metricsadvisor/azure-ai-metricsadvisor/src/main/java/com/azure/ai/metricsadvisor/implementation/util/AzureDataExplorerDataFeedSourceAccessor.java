// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.implementation.util;

import com.azure.ai.metricsadvisor.administration.models.AzureDataExplorerDataFeedSource;

public final class AzureDataExplorerDataFeedSourceAccessor {
    private static Accessor accessor;

    private AzureDataExplorerDataFeedSourceAccessor() {
    }

    /**
     * Type defining the methods to set the non-public properties of
     * an {@link AzureDataExplorerDataFeedSource} instance.
     */
    public interface Accessor {
        String getConnectionString(AzureDataExplorerDataFeedSource feedSource);
    }

    /**
     * The method called from {@link AzureDataExplorerDataFeedSource} to set it's accessor.
     *
     * @param accessor The accessor.
     */
    public static void setAccessor(final Accessor accessor) {
        AzureDataExplorerDataFeedSourceAccessor.accessor = accessor;
    }

    public static String getConnectionString(AzureDataExplorerDataFeedSource feedSource) {
        return accessor.getConnectionString(feedSource);
    }
}
