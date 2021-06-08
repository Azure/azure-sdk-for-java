// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.implementation.util;

import com.azure.ai.metricsadvisor.administration.models.DataFeed;
import com.azure.ai.metricsadvisor.administration.models.DataFeedSourceType;
import com.azure.ai.metricsadvisor.administration.models.DataFeedStatus;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * The helper class to set the non-public properties of an {@link DataFeed} instance.
 */
public final class DataFeedHelper {
    private static DataFeedAccessor accessor;

    private DataFeedHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link DataFeed} instance.
     */
    public interface DataFeedAccessor {
        void setId(DataFeed feed, String id);
        void setMetricIds(DataFeed feed, Map<String, String> metricIds);
        void setCreatedTime(DataFeed feed, OffsetDateTime createdTime);
        void setStatus(DataFeed feed, DataFeedStatus dataFeedStatus);
        void setSourceType(DataFeed feed, DataFeedSourceType dataFeedSourceType);
        void setIsAdmin(DataFeed feed, boolean isAdmin);
        void setCreator(DataFeed feed, String creator);
    }

    /**
     * The method called from {@link DataFeed} to set it's accessor.
     *
     * @param feedAccessor The accessor.
     */
    public static void setAccessor(final DataFeedAccessor feedAccessor) {
        accessor = feedAccessor;
    }

    static void setId(DataFeed feed, String id) {
        accessor.setId(feed, id);
    }

    static void setMetricIds(DataFeed feed, Map<String, String> metricIds) {
        accessor.setMetricIds(feed, metricIds);
    }

    static void setCreatedTime(DataFeed feed, OffsetDateTime createdTime) {
        accessor.setCreatedTime(feed, createdTime);
    }

    static void setStatus(DataFeed feed, DataFeedStatus dataFeedStatus) {
        accessor.setStatus(feed, dataFeedStatus);
    }

    static void setSourceType(DataFeed feed, DataFeedSourceType dataFeedSourceType) {
        accessor.setSourceType(feed, dataFeedSourceType);
    }

    static void setIsAdmin(DataFeed feed, boolean isAdmin) {
        accessor.setIsAdmin(feed, isAdmin);
    }

    static void setCreator(DataFeed feed, String creator) {
        accessor.setCreator(feed, creator);
    }
}
