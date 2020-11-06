// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.implementation.util;

import com.azure.ai.metricsadvisor.models.DimensionKey;

import java.util.List;
import com.azure.ai.metricsadvisor.models.IncidentRootCause;

/**
 * The helper class to set the non-public properties of an {@link IncidentRootCause} instance.
 */
public final class IncidentRootCauseHelper {
    private static IncidentRootCauseAccessor accessor;

    private IncidentRootCauseHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link IncidentRootCause} instance.
     */
    public interface IncidentRootCauseAccessor {
        void setSeriesKey(IncidentRootCause rootCause, DimensionKey seriesKey);
        void setPaths(IncidentRootCause rootCause, List<String> paths);
        void setContributionScore(IncidentRootCause rootCause, double confidenceScore);
        void setDescription(IncidentRootCause rootCause, String description);
    }

    /**
     * The method called from {@link IncidentRootCause} to set it's accessor.
     *
     * @param incidentRootCauseAccessor The accessor.
     */
    public static void setAccessor(final IncidentRootCauseAccessor incidentRootCauseAccessor) {
        accessor = incidentRootCauseAccessor;
    }

    static void setSeriesKey(IncidentRootCause rootCause, DimensionKey seriesKey) {
        accessor.setSeriesKey(rootCause, seriesKey);
    }

    static void setPaths(IncidentRootCause rootCause, List<String> paths) {
        accessor.setPaths(rootCause, paths);
    }

    static void setContributionScore(IncidentRootCause rootCause, double confidenceScore) {
        accessor.setContributionScore(rootCause, confidenceScore);
    }

    static void setDescription(IncidentRootCause rootCause, String description) {
        accessor.setDescription(rootCause, description);
    }
}
