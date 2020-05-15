package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.HighWaterMarkChangeDetectionPolicy;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.HighWaterMarkChangeDetectionPolicy} and
 * {@link HighWaterMarkChangeDetectionPolicy} mismatch.
 */
public final class HighWaterMarkChangeDetectionPolicyConverter {
    public static HighWaterMarkChangeDetectionPolicy convert(com.azure.search.documents.models.HighWaterMarkChangeDetectionPolicy obj) {
        return DefaultConverter.convert(obj, HighWaterMarkChangeDetectionPolicy.class);
    }

    public static com.azure.search.documents.models.HighWaterMarkChangeDetectionPolicy convert(HighWaterMarkChangeDetectionPolicy obj) {
        return DefaultConverter.convert(obj,
            com.azure.search.documents.models.HighWaterMarkChangeDetectionPolicy.class);
    }
}
