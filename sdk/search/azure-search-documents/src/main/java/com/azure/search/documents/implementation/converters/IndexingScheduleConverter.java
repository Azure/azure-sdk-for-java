// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.IndexingSchedule;

import java.time.Duration;
import java.time.OffsetDateTime;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.IndexingSchedule} and
 * {@link IndexingSchedule}.
 */
public final class IndexingScheduleConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.IndexingSchedule} to {@link IndexingSchedule}.
     */
    public static IndexingSchedule map(com.azure.search.documents.indexes.implementation.models.IndexingSchedule obj) {
        if (obj == null) {
            return null;
        }
        IndexingSchedule indexingSchedule = new IndexingSchedule();

        Duration interval = obj.getInterval();
        indexingSchedule.setInterval(interval);

        OffsetDateTime startTime = obj.getStartTime();
        indexingSchedule.setStartTime(startTime);
        return indexingSchedule;
    }

    /**
     * Maps from {@link IndexingSchedule} to {@link com.azure.search.documents.indexes.implementation.models.IndexingSchedule}.
     */
    public static com.azure.search.documents.indexes.implementation.models.IndexingSchedule map(IndexingSchedule obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.indexes.implementation.models.IndexingSchedule indexingSchedule =
            new com.azure.search.documents.indexes.implementation.models.IndexingSchedule();

        Duration interval = obj.getInterval();
        indexingSchedule.setInterval(interval);

        OffsetDateTime startTime = obj.getStartTime();
        indexingSchedule.setStartTime(startTime);
        return indexingSchedule;
    }

    private IndexingScheduleConverter() {
    }
}
