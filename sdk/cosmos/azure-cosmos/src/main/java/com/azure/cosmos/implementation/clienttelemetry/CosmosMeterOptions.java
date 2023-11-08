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

    private final boolean isDiagnosticThresholdsFilteringEnabled;

    public CosmosMeterOptions(
        CosmosMetricName name,
        boolean isEnabled,
        double[] percentiles,
        boolean isHistogramPublishingEnabled,
        EnumSet<TagName> suppressedTagNames,
        boolean isDiagnosticThresholdsFilteringEnabled) {

        this.name = name;
        this.isEnabled = isEnabled;
        this.percentiles = percentiles != null ? percentiles.clone() : new double[0];
        this.isHistogramPublishingEnabled = isHistogramPublishingEnabled;
        this.suppressedTagNames = suppressedTagNames;
        this.isDiagnosticThresholdsFilteringEnabled = isDiagnosticThresholdsFilteringEnabled;
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
        return this.percentiles.clone();
    }

    public boolean isEnabled() {
        return this.isEnabled;
    }

    public boolean isDiagnosticThresholdsFilteringEnabled() { return this.isDiagnosticThresholdsFilteringEnabled; }
}
