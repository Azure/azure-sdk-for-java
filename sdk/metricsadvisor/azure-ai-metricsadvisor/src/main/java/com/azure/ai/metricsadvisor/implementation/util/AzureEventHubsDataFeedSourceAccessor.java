// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.implementation.util;

import com.azure.ai.metricsadvisor.administration.models.AzureEventHubsDataFeedSource;
import com.azure.ai.metricsadvisor.administration.models.SqlServerDataFeedSource;

public final class AzureEventHubsDataFeedSourceAccessor {
    private static Accessor accessor;

    private AzureEventHubsDataFeedSourceAccessor() {
    }

    /**
     * Type defining the methods to set the non-public properties of
     * an {@link SqlServerDataFeedSource} instance.
     */
    public interface Accessor {
        String getConnectionString(AzureEventHubsDataFeedSource feedSource);
    }

    /**
     * The method called from {@link SqlServerDataFeedSource} to set it's accessor.
     *
     * @param accessor The accessor.
     */
    public static void setAccessor(final Accessor accessor) {
        AzureEventHubsDataFeedSourceAccessor.accessor = accessor;
    }

    public static String getConnectionString(AzureEventHubsDataFeedSource feedSource) {
        return accessor.getConnectionString(feedSource);
    }
}
