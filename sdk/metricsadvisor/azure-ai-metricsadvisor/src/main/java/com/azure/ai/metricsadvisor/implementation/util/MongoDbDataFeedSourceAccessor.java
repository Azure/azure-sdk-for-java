// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.implementation.util;

import com.azure.ai.metricsadvisor.administration.models.MongoDbDataFeedSource;

public final class MongoDbDataFeedSourceAccessor {
    private static Accessor accessor;

    private MongoDbDataFeedSourceAccessor() {
    }

    /**
     * Type defining the methods to set the non-public properties of
     * an {@link MongoDbDataFeedSource} instance.
     */
    public interface Accessor {
        String getConnectionString(MongoDbDataFeedSource feedSource);
    }

    /**
     * The method called from {@link MongoDbDataFeedSource} to set it's accessor.
     *
     * @param accessor The accessor.
     */
    public static void setAccessor(final Accessor accessor) {
        MongoDbDataFeedSourceAccessor.accessor = accessor;
    }

    public static String getConnectionString(MongoDbDataFeedSource feedSource) {
        return accessor.getConnectionString(feedSource);
    }
}
