// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.indexes.models.HighWaterMarkChangeDetectionPolicy;
import com.azure.search.documents.indexes.models.SqlIntegratedChangeTrackingPolicy;
import com.azure.search.documents.indexes.models.DataChangeDetectionPolicy;

import static com.azure.search.documents.implementation.util.Constants.ABSTRACT_EXTERNAL_ERROR_MSG;
import static com.azure.search.documents.implementation.util.Constants.ABSTRACT_INTERNAL_ERROR_MSG;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.DataChangeDetectionPolicy} and
 * {@link DataChangeDetectionPolicy}.
 */
public final class DataChangeDetectionPolicyConverter {
    private static final ClientLogger LOGGER = new ClientLogger(DataChangeDetectionPolicyConverter.class);

    /**
     * Maps abstract class from {@link com.azure.search.documents.indexes.implementation.models.DataChangeDetectionPolicy} to
     * {@link DataChangeDetectionPolicy}. Dedicate works to sub class converter.
     */
    public static DataChangeDetectionPolicy map(com.azure.search.documents.indexes.implementation.models.DataChangeDetectionPolicy obj) {
        if (obj instanceof com.azure.search.documents.indexes.implementation.models.HighWaterMarkChangeDetectionPolicy) {
            return HighWaterMarkChangeDetectionPolicyConverter.map((com.azure.search.documents.indexes.implementation.models.HighWaterMarkChangeDetectionPolicy) obj);
        }
        if (obj instanceof com.azure.search.documents.indexes.implementation.models.SqlIntegratedChangeTrackingPolicy) {
            return SqlIntegratedChangeTrackingPolicyConverter.map((com.azure.search.documents.indexes.implementation.models.SqlIntegratedChangeTrackingPolicy) obj);
        }
        throw LOGGER.logExceptionAsError(new RuntimeException(String.format(ABSTRACT_EXTERNAL_ERROR_MSG,
            obj.getClass().getSimpleName())));
    }

    /**
     * Maps abstract class from {@link DataChangeDetectionPolicy} to
     * {@link com.azure.search.documents.indexes.implementation.models.DataChangeDetectionPolicy}. Dedicate works to sub class
     * converter.
     */
    public static com.azure.search.documents.indexes.implementation.models.DataChangeDetectionPolicy map(DataChangeDetectionPolicy obj) {
        if (obj instanceof SqlIntegratedChangeTrackingPolicy) {
            return SqlIntegratedChangeTrackingPolicyConverter.map((SqlIntegratedChangeTrackingPolicy) obj);
        }
        if (obj instanceof HighWaterMarkChangeDetectionPolicy) {
            return HighWaterMarkChangeDetectionPolicyConverter.map((HighWaterMarkChangeDetectionPolicy) obj);
        }
        throw LOGGER.logExceptionAsError(new RuntimeException(String.format(ABSTRACT_INTERNAL_ERROR_MSG,
            obj.getClass().getSimpleName())));
    }

    private DataChangeDetectionPolicyConverter() {
    }
}
