// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.implementation.util;

import com.azure.ai.metricsadvisor.administration.models.SqlServerDataFeedSource;

public final class SqlServerDataFeedSourceAccessor {
    private static Accessor accessor;

    private SqlServerDataFeedSourceAccessor() {
    }

    /**
     * Type defining the methods to set the non-public properties of
     * an {@link SqlServerDataFeedSource} instance.
     */
    public interface Accessor {
        String getConnectionString(SqlServerDataFeedSource feedSource);
    }

    /**
     * The method called from {@link SqlServerDataFeedSource} to set it's accessor.
     *
     * @param accessor The accessor.
     */
    public static void setAccessor(final Accessor accessor) {
        SqlServerDataFeedSourceAccessor.accessor = accessor;
    }

    public static String getConnectionString(SqlServerDataFeedSource feedSource) {
        return accessor.getConnectionString(feedSource);
    }
}
