// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.implementation.util;

import com.azure.ai.metricsadvisor.administration.models.AzureTableDataFeedSource;
import com.azure.ai.metricsadvisor.administration.models.SqlServerDataFeedSource;

public final class AzureTableDataFeedSourceAccessor {
    private static Accessor accessor;

    private AzureTableDataFeedSourceAccessor() {
    }

    /**
     * Type defining the methods to set the non-public properties of
     * an {@link AzureTableDataFeedSource} instance.
     */
    public interface Accessor {
        String getConnectionString(AzureTableDataFeedSource feedSource);
    }

    /**
     * The method called from {@link SqlServerDataFeedSource} to set it's accessor.
     *
     * @param accessor The accessor.
     */
    public static void setAccessor(final Accessor accessor) {
        AzureTableDataFeedSourceAccessor.accessor = accessor;
    }

    public static String getConnectionString(AzureTableDataFeedSource feedSource) {
        return accessor.getConnectionString(feedSource);
    }
}
