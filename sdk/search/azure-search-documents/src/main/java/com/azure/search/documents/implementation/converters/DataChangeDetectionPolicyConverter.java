package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.DataChangeDetectionPolicy;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.DataChangeDetectionPolicy} and
 * {@link DataChangeDetectionPolicy} mismatch.
 */
public final class DataChangeDetectionPolicyConverter {
    public static DataChangeDetectionPolicy convert(com.azure.search.documents.models.DataChangeDetectionPolicy obj) {
        return DefaultConverter.convert(obj, DataChangeDetectionPolicy.class);
    }

    public static com.azure.search.documents.models.DataChangeDetectionPolicy convert(DataChangeDetectionPolicy obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.DataChangeDetectionPolicy.class);
    }
}
