// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.HighWaterMarkChangeDetectionPolicy;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.HighWaterMarkChangeDetectionPolicy}
 * and {@link HighWaterMarkChangeDetectionPolicy}.
 */
public final class HighWaterMarkChangeDetectionPolicyConverter {
    private static final ClientLogger LOGGER = new ClientLogger(HighWaterMarkChangeDetectionPolicyConverter.class);

    /**
     * Maps from {@link com.azure.search.documents.implementation.models.HighWaterMarkChangeDetectionPolicy} to
     * {@link HighWaterMarkChangeDetectionPolicy}.
     */
    public static HighWaterMarkChangeDetectionPolicy map(com.azure.search.documents.implementation.models.HighWaterMarkChangeDetectionPolicy obj) {
        if (obj == null) {
            return null;
        }
        HighWaterMarkChangeDetectionPolicy highWaterMarkChangeDetectionPolicy =
            new HighWaterMarkChangeDetectionPolicy();

        String _highWaterMarkColumnName = obj.getHighWaterMarkColumnName();
        highWaterMarkChangeDetectionPolicy.setHighWaterMarkColumnName(_highWaterMarkColumnName);
        return highWaterMarkChangeDetectionPolicy;
    }

    /**
     * Maps from {@link HighWaterMarkChangeDetectionPolicy} to
     * {@link com.azure.search.documents.implementation.models.HighWaterMarkChangeDetectionPolicy}.
     */
    public static com.azure.search.documents.implementation.models.HighWaterMarkChangeDetectionPolicy map(HighWaterMarkChangeDetectionPolicy obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.HighWaterMarkChangeDetectionPolicy highWaterMarkChangeDetectionPolicy = new com.azure.search.documents.implementation.models.HighWaterMarkChangeDetectionPolicy();

        String _highWaterMarkColumnName = obj.getHighWaterMarkColumnName();
        highWaterMarkChangeDetectionPolicy.setHighWaterMarkColumnName(_highWaterMarkColumnName);
        return highWaterMarkChangeDetectionPolicy;
    }
}
