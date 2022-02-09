// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.implementation.util;

import com.azure.ai.metricsadvisor.administration.models.MySqlDataFeedSource;
import com.azure.ai.metricsadvisor.administration.models.SqlServerDataFeedSource;

public final class MySqlDataFeedSourceAccessor {
    private static Accessor accessor;

    private MySqlDataFeedSourceAccessor() {
    }

    /**
     * Type defining the methods to set the non-public properties of
     * an {@link SqlServerDataFeedSource} instance.
     */
    public interface Accessor {
        String getConnectionString(MySqlDataFeedSource feedSource);
    }

    /**
     * The method called from {@link MySqlDataFeedSource} to set it's accessor.
     *
     * @param accessor The accessor.
     */
    public static void setAccessor(final Accessor accessor) {
        MySqlDataFeedSourceAccessor.accessor = accessor;
    }

    public static String getConnectionString(MySqlDataFeedSource feedSource) {
        return accessor.getConnectionString(feedSource);
    }
}
