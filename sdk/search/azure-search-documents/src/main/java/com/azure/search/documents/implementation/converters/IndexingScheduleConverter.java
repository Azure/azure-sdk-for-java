package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.IndexingSchedule;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.IndexingSchedule} and
 * {@link IndexingSchedule} mismatch.
 */
public final class IndexingScheduleConverter {
    public static IndexingSchedule convert(com.azure.search.documents.models.IndexingSchedule obj) {
        return DefaultConverter.convert(obj, IndexingSchedule.class);
    }

    public static com.azure.search.documents.models.IndexingSchedule convert(IndexingSchedule obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.IndexingSchedule.class);
    }
}
