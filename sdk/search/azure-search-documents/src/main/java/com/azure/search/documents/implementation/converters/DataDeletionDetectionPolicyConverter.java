package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.DataDeletionDetectionPolicy;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.DataDeletionDetectionPolicy} and
 * {@link DataDeletionDetectionPolicy} mismatch.
 */
public final class DataDeletionDetectionPolicyConverter {
    public static DataDeletionDetectionPolicy convert(com.azure.search.documents.models.DataDeletionDetectionPolicy obj) {
        return DefaultConverter.convert(obj, DataDeletionDetectionPolicy.class);
    }

    public static com.azure.search.documents.models.DataDeletionDetectionPolicy convert(DataDeletionDetectionPolicy obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.DataDeletionDetectionPolicy.class);
    }
}
