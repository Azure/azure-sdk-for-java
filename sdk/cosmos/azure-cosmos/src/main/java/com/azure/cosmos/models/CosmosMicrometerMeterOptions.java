// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.clienttelemetry.TagName;

import java.util.EnumSet;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Options of a Cosmos client-side meter that can be used to enable/disable it, change the percentile and histogram
 * capturing (if percentiles are applicable for the meter) and allows suppressing tags that are not desired.
 */
public final class CosmosMicrometerMeterOptions {

    private final CosmosMetricName meterName;
    private boolean isHistogramPublishingEnabled;
    private double[] percentiles;
    private EnumSet<TagName> suppressedTagNames;
    private boolean isEnabled;

    /**
     * Instantiates new options for a specific Cosmos DB meter
     */
    CosmosMicrometerMeterOptions(
        CosmosMetricName meterName,
        boolean isHistogramPublishingEnabled,
        double[] percentiles) {

        checkNotNull(meterName, "Argument 'meterName' must not be null.");

        this.meterName = meterName;
        this.isHistogramPublishingEnabled = isHistogramPublishingEnabled;
        this.percentiles = percentiles;
        this.suppressedTagNames = EnumSet.noneOf(TagName.class);
        this.isEnabled = true;
    }

    /**
     * Gets the name of the meter these options are applicable for
     * @return the meter name for these options
     */
    public CosmosMetricName getMeterName() {
        return this.meterName;
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
            this.percentiles = null;
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

    /**
     * Flag indicating if this meter is currently enabled.
     * @return {@code true} if meter is currently enabled, {@code false} otherwise.
     */
    public boolean isEnabled() {
        return this.isEnabled;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    // the following helper/accessor only helps to access this class outside of this package.//
    ///////////////////////////////////////////////////////////////////////////////////////////
    static void initialize() {
        ImplementationBridgeHelpers.CosmosMeterOptionsHelper.setCosmosMeterOptionsAccessor(
            new ImplementationBridgeHelpers.CosmosMeterOptionsHelper.CosmosMeterOptionsAccessor() {
                @Override
                public EnumSet<TagName> getSuppressedTagNames(CosmosMicrometerMeterOptions options) {
                    return options.suppressedTagNames;
                }

                @Override
                public boolean isHistogramPublishingEnabled(CosmosMicrometerMeterOptions options) {
                    return options.isHistogramPublishingEnabled;
                }

                @Override
                public double[] getPercentiles(CosmosMicrometerMeterOptions options) {
                    return options.percentiles;
                }
            }
        );
    }
}
