// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.SoftDeleteColumnDeletionDetectionPolicy;

/**
 * A converter between
 * {@link com.azure.search.documents.implementation.models.SoftDeleteColumnDeletionDetectionPolicy} and
 * {@link SoftDeleteColumnDeletionDetectionPolicy}.
 */
public final class SoftDeleteColumnDeletionDetectionPolicyConverter {
    private static final ClientLogger LOGGER = new ClientLogger(SoftDeleteColumnDeletionDetectionPolicyConverter.class);

    /**
     * Maps from {@link com.azure.search.documents.implementation.models.SoftDeleteColumnDeletionDetectionPolicy} to
     * {@link SoftDeleteColumnDeletionDetectionPolicy}.
     */
    public static SoftDeleteColumnDeletionDetectionPolicy map(com.azure.search.documents.implementation.models.SoftDeleteColumnDeletionDetectionPolicy obj) {
        if (obj == null) {
            return null;
        }
        SoftDeleteColumnDeletionDetectionPolicy softDeleteColumnDeletionDetectionPolicy =
            new SoftDeleteColumnDeletionDetectionPolicy();

        String _softDeleteColumnName = obj.getSoftDeleteColumnName();
        softDeleteColumnDeletionDetectionPolicy.setSoftDeleteColumnName(_softDeleteColumnName);

        String _softDeleteMarkerValue = obj.getSoftDeleteMarkerValue();
        softDeleteColumnDeletionDetectionPolicy.setSoftDeleteMarkerValue(_softDeleteMarkerValue);
        return softDeleteColumnDeletionDetectionPolicy;
    }

    /**
     * Maps from {@link SoftDeleteColumnDeletionDetectionPolicy} to
     * {@link com.azure.search.documents.implementation.models.SoftDeleteColumnDeletionDetectionPolicy}.
     */
    public static com.azure.search.documents.implementation.models.SoftDeleteColumnDeletionDetectionPolicy map(SoftDeleteColumnDeletionDetectionPolicy obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.SoftDeleteColumnDeletionDetectionPolicy softDeleteColumnDeletionDetectionPolicy = new com.azure.search.documents.implementation.models.SoftDeleteColumnDeletionDetectionPolicy();

        String _softDeleteColumnName = obj.getSoftDeleteColumnName();
        softDeleteColumnDeletionDetectionPolicy.setSoftDeleteColumnName(_softDeleteColumnName);

        String _softDeleteMarkerValue = obj.getSoftDeleteMarkerValue();
        softDeleteColumnDeletionDetectionPolicy.setSoftDeleteMarkerValue(_softDeleteMarkerValue);
        return softDeleteColumnDeletionDetectionPolicy;
    }
}
