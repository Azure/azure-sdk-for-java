// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.implementation.util;

import com.azure.ai.metricsadvisor.administration.models.AzureLogAnalyticsDataFeedSource;
import com.azure.ai.metricsadvisor.administration.models.SqlServerDataFeedSource;

public final class AzureLogAnalyticsDataFeedSourceAccessor {
    private static Accessor accessor;

    private AzureLogAnalyticsDataFeedSourceAccessor() {
    }

    /**
     * Type defining the methods to set the non-public properties of
     * an {@link SqlServerDataFeedSource} instance.
     */
    public interface Accessor {
        String getClientSecret(AzureLogAnalyticsDataFeedSource feedSource);
    }

    /**
     * The method called from {@link AzureLogAnalyticsDataFeedSource} to set it's accessor.
     *
     * @param accessor The accessor.
     */
    public static void setAccessor(final Accessor accessor) {
        AzureLogAnalyticsDataFeedSourceAccessor.accessor = accessor;
    }

    public static String getClientSecret(AzureLogAnalyticsDataFeedSource feedSource) {
        return accessor.getClientSecret(feedSource);
    }
}
