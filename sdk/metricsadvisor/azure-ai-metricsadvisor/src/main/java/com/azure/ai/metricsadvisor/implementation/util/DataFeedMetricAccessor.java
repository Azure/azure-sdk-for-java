// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.implementation.util;

import com.azure.ai.metricsadvisor.administration.models.DataFeedMetric;

public final class DataFeedMetricAccessor {
    private static Accessor accessor;

    private DataFeedMetricAccessor() {
    }

    public interface Accessor {
        void setId(DataFeedMetric dataFeedMetric, String id);
    }

    /**
     * The method called from {@link DataFeedMetric} to set it's accessor.
     *
     * @param accessor The accessor.
     */
    public static void setAccessor(final Accessor accessor) {
        DataFeedMetricAccessor.accessor = accessor;
    }

    public static void setId(DataFeedMetric dataFeedMetric, String id) {
        accessor.setId(dataFeedMetric, id);
    }
}
