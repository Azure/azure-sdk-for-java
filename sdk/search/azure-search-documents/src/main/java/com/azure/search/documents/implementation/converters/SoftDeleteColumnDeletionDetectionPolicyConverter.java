package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.SoftDeleteColumnDeletionDetectionPolicy;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if
 * {@link com.azure.search.documents.models.SoftDeleteColumnDeletionDetectionPolicy} and
 * {@link SoftDeleteColumnDeletionDetectionPolicy} mismatch.
 */
public final class SoftDeleteColumnDeletionDetectionPolicyConverter {
    public static SoftDeleteColumnDeletionDetectionPolicy convert(com.azure.search.documents.models.SoftDeleteColumnDeletionDetectionPolicy obj) {
        return DefaultConverter.convert(obj, SoftDeleteColumnDeletionDetectionPolicy.class);
    }

    public static com.azure.search.documents.models.SoftDeleteColumnDeletionDetectionPolicy convert(SoftDeleteColumnDeletionDetectionPolicy obj) {
        return DefaultConverter.convert(obj,
            com.azure.search.documents.models.SoftDeleteColumnDeletionDetectionPolicy.class);
    }
}
