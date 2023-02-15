// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.core.util.MetricsOptions;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.clienttelemetry.TagName;

import java.util.EnumSet;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public final class CosmosMeterOptions extends MetricsOptions {

    private final CosmosMeterName meterName;
    private boolean isHistogramPublishingEnabled;
    private double[] percentiles;
    private EnumSet<TagName> suppressedTagNames;

    /**
     * Instantiates new options for a specific Cosmos DB meter
     */
    CosmosMeterOptions(
        CosmosMeterName meterName,
        boolean isHistogramPublishingEnabled,
        double[] percentiles) {

        checkNotNull(meterName, "Argument 'meterName' must not be null.");

        this.meterName = meterName;
        this.isHistogramPublishingEnabled = isHistogramPublishingEnabled;
        this.percentiles = percentiles;
        this.suppressedTagNames = EnumSet.noneOf(TagName.class);
    }

    public CosmosMeterName getMeterName() {
        return this.meterName;
    }

    /**
     * Sets the tags that should be used for this meter (when applicable)
     *
     * @param tags - the tags to be used (when applicable) for this meter
     * @return current CosmosMeterOptions instance
     */
    public CosmosMeterOptions suppressTagNames(CosmosMeterTagName... tags) {
        EnumSet<TagName> newTagNames = EnumSet.noneOf(TagName.class);
        if (tags != null && tags.length > 0) {
            for (CosmosMeterTagName t: tags) {
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
    public CosmosMeterOptions histogramPublishingEnabled(boolean isEnabled) {
        this.isHistogramPublishingEnabled = isEnabled;

        return this;
    }

    /**
     * Sets the percentiles that should be captured for this meter (when applicable)
     *
     * @param percentiles - a flag indicating whether histogram publishing is enabled for this meter
     * @return current CosmosMeterOptions instance
     */
    public CosmosMeterOptions percentiles(double... percentiles) {
        if (percentiles == null || percentiles.length == 0) {
            this.percentiles = null;
        } else {
            this.percentiles = percentiles.clone();
        }

        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CosmosMeterOptions setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        return this;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    // the following helper/accessor only helps to access this class outside of this package.//
    ///////////////////////////////////////////////////////////////////////////////////////////
    static void initialize() {
        ImplementationBridgeHelpers.CosmosMeterOptionsHelper.setCosmosMeterOptionsAccessor(
            new ImplementationBridgeHelpers.CosmosMeterOptionsHelper.CosmosMeterOptionsAccessor() {
                @Override
                public EnumSet<TagName> getSuppressedTagNames(CosmosMeterOptions options) {
                    return options.suppressedTagNames;
                }

                @Override
                public boolean isHistogramPublishingEnabled(CosmosMeterOptions options) {
                    return options.isHistogramPublishingEnabled;
                }

                @Override
                public double[] getPercentiles(CosmosMeterOptions options) {
                    return options.percentiles;
                }
            }
        );
    }
}
