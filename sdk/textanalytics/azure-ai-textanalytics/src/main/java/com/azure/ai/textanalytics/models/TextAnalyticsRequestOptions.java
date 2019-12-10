// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Fluent;

/**
 * The TextAnalyticsRequestOptions model.
 */
@Fluent
public final class TextAnalyticsRequestOptions {
    private String modelVersion;
    private boolean showStatistics;

    public String getModelVersion() {
        return modelVersion;
    }

    public TextAnalyticsRequestOptions setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
        return this;
    }

    public boolean showStatistics() {
        return showStatistics;
    }

    public TextAnalyticsRequestOptions setShowStatistics(boolean showStatistics) {
        this.showStatistics = showStatistics;
        return this;
    }
}
