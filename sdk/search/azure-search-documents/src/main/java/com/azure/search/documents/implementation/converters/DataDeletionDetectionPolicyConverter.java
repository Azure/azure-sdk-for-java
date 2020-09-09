// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.indexes.models.SoftDeleteColumnDeletionDetectionPolicy;
import com.azure.search.documents.indexes.models.DataDeletionDetectionPolicy;

import static com.azure.search.documents.implementation.util.Constants.ABSTRACT_EXTERNAL_ERROR_MSG;
import static com.azure.search.documents.implementation.util.Constants.ABSTRACT_INTERNAL_ERROR_MSG;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.DataDeletionDetectionPolicy} and
 * {@link DataDeletionDetectionPolicy}.
 */
public final class DataDeletionDetectionPolicyConverter {
    private static final ClientLogger LOGGER = new ClientLogger(DataDeletionDetectionPolicyConverter.class);

    /**
     * Maps abstract class from {@link com.azure.search.documents.indexes.implementation.models.DataDeletionDetectionPolicy} to
     * {@link DataDeletionDetectionPolicy}. Dedicate works to sub class converter.
     */
    public static DataDeletionDetectionPolicy map(com.azure.search.documents.indexes.implementation.models.DataDeletionDetectionPolicy obj) {
        if (obj instanceof com.azure.search.documents.indexes.implementation.models.SoftDeleteColumnDeletionDetectionPolicy) {
            return SoftDeleteColumnDeletionDetectionPolicyConverter.map((com.azure.search.documents.indexes.implementation.models.SoftDeleteColumnDeletionDetectionPolicy) obj);
        }
        throw LOGGER.logExceptionAsError(new RuntimeException(String.format(ABSTRACT_EXTERNAL_ERROR_MSG,
            obj.getClass().getSimpleName())));
    }

    /**
     * Maps abstract class from {@link DataDeletionDetectionPolicy} to
     * {@link com.azure.search.documents.indexes.implementation.models.DataDeletionDetectionPolicy}. Dedicate works to sub
     * class converter.
     */
    public static com.azure.search.documents.indexes.implementation.models.DataDeletionDetectionPolicy map(DataDeletionDetectionPolicy obj) {
        if (obj instanceof SoftDeleteColumnDeletionDetectionPolicy) {
            return SoftDeleteColumnDeletionDetectionPolicyConverter.map((SoftDeleteColumnDeletionDetectionPolicy) obj);
        }
        throw LOGGER.logExceptionAsError(new RuntimeException(String.format(ABSTRACT_INTERNAL_ERROR_MSG,
            obj.getClass().getSimpleName())));
    }

    private DataDeletionDetectionPolicyConverter() {
    }
}
