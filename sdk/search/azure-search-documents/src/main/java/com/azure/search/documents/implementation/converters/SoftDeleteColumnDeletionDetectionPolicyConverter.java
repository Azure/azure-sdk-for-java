// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.SoftDeleteColumnDeletionDetectionPolicy;

/**
 * A converter between
 * {@link com.azure.search.documents.indexes.implementation.models.SoftDeleteColumnDeletionDetectionPolicy} and
 * {@link SoftDeleteColumnDeletionDetectionPolicy}.
 */
public final class SoftDeleteColumnDeletionDetectionPolicyConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.SoftDeleteColumnDeletionDetectionPolicy} to
     * {@link SoftDeleteColumnDeletionDetectionPolicy}.
     */
    public static SoftDeleteColumnDeletionDetectionPolicy map(com.azure.search.documents.indexes.implementation.models.SoftDeleteColumnDeletionDetectionPolicy obj) {
        if (obj == null) {
            return null;
        }
        SoftDeleteColumnDeletionDetectionPolicy softDeleteColumnDeletionDetectionPolicy =
            new SoftDeleteColumnDeletionDetectionPolicy();

        String softDeleteColumnName = obj.getSoftDeleteColumnName();
        softDeleteColumnDeletionDetectionPolicy.setSoftDeleteColumnName(softDeleteColumnName);

        String softDeleteMarkerValue = obj.getSoftDeleteMarkerValue();
        softDeleteColumnDeletionDetectionPolicy.setSoftDeleteMarkerValue(softDeleteMarkerValue);
        return softDeleteColumnDeletionDetectionPolicy;
    }

    /**
     * Maps from {@link SoftDeleteColumnDeletionDetectionPolicy} to
     * {@link com.azure.search.documents.indexes.implementation.models.SoftDeleteColumnDeletionDetectionPolicy}.
     */
    public static com.azure.search.documents.indexes.implementation.models.SoftDeleteColumnDeletionDetectionPolicy map(SoftDeleteColumnDeletionDetectionPolicy obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.indexes.implementation.models.SoftDeleteColumnDeletionDetectionPolicy softDeleteColumnDeletionDetectionPolicy = new com.azure.search.documents.indexes.implementation.models.SoftDeleteColumnDeletionDetectionPolicy();

        String softDeleteColumnName = obj.getSoftDeleteColumnName();
        softDeleteColumnDeletionDetectionPolicy.setSoftDeleteColumnName(softDeleteColumnName);

        String softDeleteMarkerValue = obj.getSoftDeleteMarkerValue();
        softDeleteColumnDeletionDetectionPolicy.setSoftDeleteMarkerValue(softDeleteMarkerValue);
        return softDeleteColumnDeletionDetectionPolicy;
    }

    private SoftDeleteColumnDeletionDetectionPolicyConverter() {
    }
}
