// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cs.textanalytics.models;

public class TextAnalyticsRequestOptions {
    private String modelVersion;
    private boolean showStats;

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    public boolean isShowStats() {
        return showStats;
    }

    public void setShowStats(boolean showStats) {
        this.showStats = showStats;
    }
}
