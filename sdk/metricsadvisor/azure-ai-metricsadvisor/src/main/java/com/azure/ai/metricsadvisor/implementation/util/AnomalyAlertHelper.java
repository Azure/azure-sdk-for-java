// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.implementation.util;

import com.azure.ai.metricsadvisor.models.AnomalyAlert;

import java.time.OffsetDateTime;

/**
 * The helper class to set the non-public properties of an {@link AnomalyAlert} instance.
 */
public final class AnomalyAlertHelper {
    private static AnomalyAlertAccessor accessor;

    private AnomalyAlertHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link AnomalyAlert} instance.
     */
    public interface AnomalyAlertAccessor {
        void setId(AnomalyAlert anomalyAlert, String id);
        void setCreatedTime(AnomalyAlert anomalyAlert, OffsetDateTime createdTime);
        void setModifiedTime(AnomalyAlert anomalyAlert, OffsetDateTime modifiedTime);
    }

    /**
     * The method called from {@link AnomalyAlert} to set it's accessor.
     *
     * @param anomalyAlertAccessor The accessor.
     */
    public static void setAccessor(final AnomalyAlertAccessor anomalyAlertAccessor) {
        accessor = anomalyAlertAccessor;
    }

    static void setId(AnomalyAlert anomalyAlert, String id) {
        accessor.setId(anomalyAlert, id);
    }

    static void setCreatedTime(AnomalyAlert anomalyAlert, OffsetDateTime createdTime) {
        accessor.setCreatedTime(anomalyAlert, createdTime);
    }

    static void setModifiedTime(AnomalyAlert anomalyAlert, OffsetDateTime modifiedTime) {
        accessor.setModifiedTime(anomalyAlert, modifiedTime);
    }
}
