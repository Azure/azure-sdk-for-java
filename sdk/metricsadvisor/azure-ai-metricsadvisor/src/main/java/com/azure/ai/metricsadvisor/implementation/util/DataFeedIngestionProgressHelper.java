// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.implementation.util;

import com.azure.ai.metricsadvisor.administration.models.DataFeedIngestionProgress;

import java.time.OffsetDateTime;

/**
 * The helper class to set the non-public properties of an {@link DataFeedIngestionProgress} instance.
 */
public final class DataFeedIngestionProgressHelper {
    private static DataFeedIngestionProgressAccessor accessor;

    private DataFeedIngestionProgressHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link DataFeedIngestionProgress} instance.
     */
    public interface DataFeedIngestionProgressAccessor {
        void setLatestActiveTimestamp(DataFeedIngestionProgress ingestionProgress, OffsetDateTime dataTime);
        void setLatestSuccessTimestamp(DataFeedIngestionProgress ingestionProgress, OffsetDateTime dataTime);
    }

    /**
     * The method called from {@link DataFeedIngestionProgress} to set it's accessor.
     *
     * @param dataFeedIngestionStatusAccessor The accessor.
     */
    public static void setAccessor(final DataFeedIngestionProgressAccessor dataFeedIngestionStatusAccessor) {
        accessor = dataFeedIngestionStatusAccessor;
    }

    public static void setLatestActiveTimestamp(DataFeedIngestionProgress ingestionProgress, OffsetDateTime dataTime) {
        accessor.setLatestActiveTimestamp(ingestionProgress, dataTime);
    }

    public static void setLatestSuccessTimestamp(DataFeedIngestionProgress ingestionProgress, OffsetDateTime dataTime) {
        accessor.setLatestSuccessTimestamp(ingestionProgress, dataTime);
    }
}

