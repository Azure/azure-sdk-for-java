// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.implementation.util;

import com.azure.ai.metricsadvisor.administration.models.AnomalyDetectionConfiguration;

/**
 * The helper class to set the non-public properties of an {@link AnomalyDetectionConfiguration} instance.
 */
public final class AnomalyDetectionConfigurationHelper {
    private static AnomalyDetectionConfigurationAccessor accessor;

    private AnomalyDetectionConfigurationHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link AnomalyDetectionConfiguration} instance.
     */
    public interface AnomalyDetectionConfigurationAccessor {
        void setId(AnomalyDetectionConfiguration configuration, String id);
        void setMetricId(AnomalyDetectionConfiguration configuration, String metricId);
    }

    /**
     * The method called from {@link AnomalyDetectionConfiguration} to set it's accessor.
     *
     * @param configurationAccessor The accessor.
     */
    public static void setAccessor(final AnomalyDetectionConfigurationAccessor configurationAccessor) {
        accessor = configurationAccessor;
    }

    static void setId(AnomalyDetectionConfiguration configuration, String id) {
        accessor.setId(configuration, id);
    }

    static void setMetricId(AnomalyDetectionConfiguration configuration, String metricId) {
        accessor.setMetricId(configuration, metricId);
    }
}

