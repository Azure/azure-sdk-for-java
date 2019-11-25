// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cs.textanalytics.models;

import com.azure.core.annotation.Fluent;

/**
 * The TextAnalyticsRequestOptions model.
 */
@Fluent
public final class TextAnalyticsRequestOptions {
    private String modelVersion;
    private boolean showStats;

    public String getModelVersion() {
        return modelVersion;
    }

    public TextAnalyticsRequestOptions setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
        return this;
    }

    public boolean isShowStatistics() {
        return showStats;
    }

    public TextAnalyticsRequestOptions setShowStatistics(boolean showStats) {
        this.showStats = showStats;
        return this;
    }
}
