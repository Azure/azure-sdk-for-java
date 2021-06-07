// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.implementation.util;

import com.azure.ai.metricsadvisor.administration.models.PostgreSqlDataFeedSource;

public final class PostgreSqlDataFeedSourceAccessor {
    private static Accessor accessor;

    private PostgreSqlDataFeedSourceAccessor() {
    }

    /**
     * Type defining the methods to set the non-public properties of
     * an {@link PostgreSqlDataFeedSource} instance.
     */
    public interface Accessor {
        String getConnectionString(PostgreSqlDataFeedSource feedSource);
    }

    /**
     * The method called from {@link PostgreSqlDataFeedSource} to set it's accessor.
     *
     * @param accessor The accessor.
     */
    public static void setAccessor(final Accessor accessor) {
        PostgreSqlDataFeedSourceAccessor.accessor = accessor;
    }

    public static String getConnectionString(PostgreSqlDataFeedSource feedSource) {
        return accessor.getConnectionString(feedSource);
    }
}
