// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.clienttelemetry;

import com.azure.cosmos.models.CosmosMetricName;

import java.util.EnumSet;

public final class CosmosMeterOptions {

    private final CosmosMetricName name;
    private final boolean isEnabled;
    private final double[] percentiles;
    private final boolean isHistogramPublishingEnabled;
    private final EnumSet<TagName> suppressedTagNames;

    public CosmosMeterOptions(
        CosmosMetricName name,
        boolean isEnabled,
        double[] percentiles,
        boolean isHistogramPublishingEnabled,
        EnumSet<TagName> suppressedTagNames) {

        this.name = name;
        this.isEnabled = isEnabled;
        this.percentiles = percentiles != null ? percentiles.clone() : null;
        this.isHistogramPublishingEnabled = isHistogramPublishingEnabled;
        this.suppressedTagNames = suppressedTagNames;
    }

    public CosmosMetricName getMeterName() {
        return this.name;
    }

    public EnumSet<TagName> getSuppressedTagNames() {
        return this.suppressedTagNames;
    }

    public boolean isHistogramPublishingEnabled() {
        return this.isHistogramPublishingEnabled;
    }

    public double[] getPercentiles() {
        return this.percentiles != null ? this.percentiles.clone() : null;
    }

    public boolean isEnabled() {
        return this.isEnabled;
    }
}
