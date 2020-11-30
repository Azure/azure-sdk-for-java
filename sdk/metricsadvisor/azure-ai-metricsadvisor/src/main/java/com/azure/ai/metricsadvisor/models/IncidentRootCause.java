// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.models;

import com.azure.ai.metricsadvisor.implementation.util.IncidentRootCauseHelper;

import java.util.List;

/**
 * The IncidentRootCause model
 **/
public final class IncidentRootCause {
    private DimensionKey seriesKey;
    private List<String> paths;
    private double contributionScore;
    private String description;

    static {
        IncidentRootCauseHelper.setAccessor(new IncidentRootCauseHelper.IncidentRootCauseAccessor() {
            @Override
            public void setSeriesKey(IncidentRootCause rootCause, DimensionKey seriesKey) {
                rootCause.setSeriesKey(seriesKey);
            }

            @Override
            public void setPaths(IncidentRootCause rootCause, List<String> paths) {
                rootCause.setPaths(paths);

            }

            @Override
            public void setContributionScore(IncidentRootCause rootCause, double confidenceScore) {
                rootCause.setContributionScore(confidenceScore);
            }

            @Override
            public void setDescription(IncidentRootCause rootCause, String description) {
                rootCause.setDescription(description);
            }
        });
    }

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
     * Get the contribution score value for the returned root cause.
     *
     * @return the score value.
     */
    public double getContributionScore() {
        return this.contributionScore;
    }

    /**
     * Get the description of this root cause.
     *
     * @return the description value.
     */
    public String getDescription() {
        return this.description;
    }

    void setSeriesKey(DimensionKey seriesKey) {
        this.seriesKey = seriesKey;
    }

    void setPaths(List<String> paths) {
        this.paths = paths;
    }

    void setContributionScore(double contributionScore) {
        this.contributionScore = contributionScore;
    }

    void setDescription(String description) {
        this.description = description;
    }
}
