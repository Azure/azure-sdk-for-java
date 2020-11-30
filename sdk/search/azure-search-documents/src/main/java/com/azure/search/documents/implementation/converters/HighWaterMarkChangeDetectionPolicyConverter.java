// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.HighWaterMarkChangeDetectionPolicy;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.HighWaterMarkChangeDetectionPolicy}
 * and {@link HighWaterMarkChangeDetectionPolicy}.
 */
public final class HighWaterMarkChangeDetectionPolicyConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.HighWaterMarkChangeDetectionPolicy} to
     * {@link HighWaterMarkChangeDetectionPolicy}.
     */
    public static HighWaterMarkChangeDetectionPolicy map(
        com.azure.search.documents.indexes.implementation.models.HighWaterMarkChangeDetectionPolicy obj) {
        if (obj == null) {
            return null;
        }
        return new HighWaterMarkChangeDetectionPolicy(obj.getHighWaterMarkColumnName());
    }

    /**
     * Maps from {@link HighWaterMarkChangeDetectionPolicy} to {@link com.azure.search.documents.indexes.implementation.models.HighWaterMarkChangeDetectionPolicy}.
     */
    public static com.azure.search.documents.indexes.implementation.models.HighWaterMarkChangeDetectionPolicy map(
        HighWaterMarkChangeDetectionPolicy obj) {
        if (obj == null) {
            return null;
        }

        return new com.azure.search.documents.indexes.implementation.models.HighWaterMarkChangeDetectionPolicy(
            obj.getHighWaterMarkColumnName());
    }

    private HighWaterMarkChangeDetectionPolicyConverter() {
    }
}
