// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.implementation.clienttelemetry.TagName;

import java.util.EnumSet;

/**
 * Options of a Cosmos client-side meter that can be used to enable/disable it, change the percentile and histogram
 * capturing (if percentiles are applicable for the meter) and allows suppressing tags that are not desired.
 */
public final class CosmosMicrometerMeterOptions {

    private Boolean isHistogramPublishingEnabled;
    private double[] percentiles;
    private EnumSet<TagName> suppressedTagNames;
    private Boolean isEnabled;

    /**
     * Instantiates new options for a specific Cosmos DB meter
     */
    public CosmosMicrometerMeterOptions() {
        this.isHistogramPublishingEnabled = null;
        this.percentiles = null;
        this.suppressedTagNames = null;
        this.isEnabled = null;
    }

    /**
     * Sets the tags that should be used for this meter (when applicable)
     *
     * @param tags - the tags to be used (when applicable) for this meter
     * @return current CosmosMeterOptions instance
     */
    public CosmosMicrometerMeterOptions suppressTagNames(CosmosMetricTagName... tags) {
        EnumSet<TagName> newTagNames = EnumSet.noneOf(TagName.class);
        if (tags != null && tags.length > 0) {
            for (CosmosMetricTagName t: tags) {
                for (TagName tagName: t.getTagNames()) {
                    if (!TagName.MINIMUM_TAGS.contains(tagName)) {
                        newTagNames.add(tagName);
                    }
                }
            }
        }

        this.suppressedTagNames = newTagNames;

        return this;
    }

    /**
     * Sets the tags that should be used for this meter (when applicable)
     *
     * @param isEnabled - a flag indicating whether histogram publishing is enabled for this meter
     * @return current CosmosMeterOptions instance
     */
    public CosmosMicrometerMeterOptions histogramPublishingEnabled(boolean isEnabled) {
        this.isHistogramPublishingEnabled = isEnabled;

        return this;
    }

    /**
     * Sets the percentiles that should be captured for this meter (when applicable)
     *
     * @param percentiles - a flag indicating whether histogram publishing is enabled for this meter
     * @return current CosmosMeterOptions instance
     */
    public CosmosMicrometerMeterOptions percentiles(double... percentiles) {
        if (percentiles == null || percentiles.length == 0) {
            this.percentiles = new double[0];
        } else {
            this.percentiles = percentiles.clone();
        }

        return this;
    }

    /**
     * Enables or disables this meter. By default, meters are enabled.
     *
     * @param enabled pass {@code true} to enable the meter.
     * @return the updated {@code MetricsOptions} object.
     */
    public CosmosMicrometerMeterOptions setEnabled(boolean enabled) {
        this.isEnabled = enabled;
        return this;
    }

    EnumSet<TagName> getSuppressedTagNames() {
        return this.suppressedTagNames;
    }

    Boolean getIsHistogramPublishingEnabled() {
        return this.isHistogramPublishingEnabled;
    }

    Boolean getIsEnabled() {
        return this.isEnabled;
    }

    double[] getPercentiles() {
        return this.percentiles;
    }
}
