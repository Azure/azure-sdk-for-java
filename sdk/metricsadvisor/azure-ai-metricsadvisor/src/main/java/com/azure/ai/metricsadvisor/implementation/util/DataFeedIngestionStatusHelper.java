// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.implementation.util;

import com.azure.ai.metricsadvisor.administration.models.DataFeedIngestionStatus;
import com.azure.ai.metricsadvisor.administration.models.IngestionStatusType;

import java.time.OffsetDateTime;

/**
 * The helper class to set the non-public properties of an {@link DataFeedIngestionStatus} instance.
 */
public final class DataFeedIngestionStatusHelper {
    private static DataFeedIngestionStatusAccessor accessor;

    private DataFeedIngestionStatusHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link DataFeedIngestionStatus} instance.
     */
    public interface DataFeedIngestionStatusAccessor {
        void setTimestamp(DataFeedIngestionStatus ingestionStatus, OffsetDateTime dataTime);
        void setMessage(DataFeedIngestionStatus ingestionStatus, String message);
        void setIngestionStatusType(DataFeedIngestionStatus ingestionStatus, IngestionStatusType ingestionStatusType);
    }

    /**
     * The method called from {@link DataFeedIngestionStatus} to set it's accessor.
     *
     * @param dataFeedIngestionStatusAccessor The accessor.
     */
    public static void setAccessor(final DataFeedIngestionStatusAccessor dataFeedIngestionStatusAccessor) {
        accessor = dataFeedIngestionStatusAccessor;
    }

    public static void setTimestamp(DataFeedIngestionStatus ingestionStatus, OffsetDateTime dataTime) {
        accessor.setTimestamp(ingestionStatus, dataTime);
    }

    public static void setMessage(DataFeedIngestionStatus ingestionStatus, String message) {
        accessor.setMessage(ingestionStatus, message);
    }

    public static void setIngestionStatusType(DataFeedIngestionStatus ingestionStatus, IngestionStatusType ingestionStatusType) {
        accessor.setIngestionStatusType(ingestionStatus, ingestionStatusType);
    }
}

