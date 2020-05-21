// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.IndexingSchedule;

import java.time.Duration;
import java.time.OffsetDateTime;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.IndexingSchedule} and
 * {@link IndexingSchedule}.
 */
public final class IndexingScheduleConverter {
    private static final ClientLogger LOGGER = new ClientLogger(IndexingScheduleConverter.class);

    /**
     * Maps from {@link com.azure.search.documents.implementation.models.IndexingSchedule} to {@link IndexingSchedule}.
     */
    public static IndexingSchedule map(com.azure.search.documents.implementation.models.IndexingSchedule obj) {
        if (obj == null) {
            return null;
        }
        IndexingSchedule indexingSchedule = new IndexingSchedule();

        Duration _interval = obj.getInterval();
        indexingSchedule.setInterval(_interval);

        OffsetDateTime _startTime = obj.getStartTime();
        indexingSchedule.setStartTime(_startTime);
        return indexingSchedule;
    }

    /**
     * Maps from {@link IndexingSchedule} to {@link com.azure.search.documents.implementation.models.IndexingSchedule}.
     */
    public static com.azure.search.documents.implementation.models.IndexingSchedule map(IndexingSchedule obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.IndexingSchedule indexingSchedule =
            new com.azure.search.documents.implementation.models.IndexingSchedule();

        Duration _interval = obj.getInterval();
        indexingSchedule.setInterval(_interval);

        OffsetDateTime _startTime = obj.getStartTime();
        indexingSchedule.setStartTime(_startTime);
        return indexingSchedule;
    }
}
