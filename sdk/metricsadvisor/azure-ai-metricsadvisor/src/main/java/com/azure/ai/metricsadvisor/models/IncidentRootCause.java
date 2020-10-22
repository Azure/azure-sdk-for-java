// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.models;

import java.util.List;

/**
 * The IncidentRootCause model
 **/
public final class IncidentRootCause {
    private DimensionKey seriesKey;
    private List<String> paths;
    private double confidenceScore;
    private String description;

    /**
     * Get the dimension name and value pair.
     * <p> A {@link DimensionKey} can hold such a combination, for example,
     * [ product_category=men-shoes, city=redmond ] identifies one specific
     * time-series.
     * </p>
     *
     * @return the seriesKey value.
     */
    public DimensionKey getSeriesKey() {
        return this.seriesKey;
    }

    /**
     * Get the list of  drilling down path from query anomaly to root cause.
     *
     * @return the path value.
     */
    public List<String> getPaths() {
        return this.paths;
    }

    /**
     * Get the confidence score value for the returned root cause.
     *
     * @return the score value.
     */
    public double getConfidenceScore() {
        return this.confidenceScore;
    }

    /**
     * Get the description of this root cause.
     *
     * @return the description value.
     */
    public String getDescription() {
        return this.description;
    }
}
