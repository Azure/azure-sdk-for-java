// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.implementation.util;

import com.azure.ai.metricsadvisor.models.InfluxDbDataFeedSource;
import com.azure.ai.metricsadvisor.administration.models.SqlServerDataFeedSource;

public final class InfluxDbDataFeedSourceAccessor {
    private static Accessor accessor;

    private InfluxDbDataFeedSourceAccessor() {
    }

    /**
     * Type defining the methods to set the non-public properties of
     * an {@link SqlServerDataFeedSource} instance.
     */
    public interface Accessor {
        String getPassword(InfluxDbDataFeedSource feedSource);
    }

    /**
     * The method called from {@link InfluxDbDataFeedSource} to set it's accessor.
     *
     * @param accessor The accessor.
     */
    public static void setAccessor(final Accessor accessor) {
        InfluxDbDataFeedSourceAccessor.accessor = accessor;
    }

    public static String getPassword(InfluxDbDataFeedSource feedSource) {
        return accessor.getPassword(feedSource);
    }
}
