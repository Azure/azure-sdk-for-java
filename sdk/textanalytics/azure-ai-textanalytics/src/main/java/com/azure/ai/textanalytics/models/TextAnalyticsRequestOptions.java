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

    /**
     * Get the model version
     *
     * @return the model version
     */
    public String getModelVersion() {
        return modelVersion;
    }

    /**
     * Set the model version
     *
     * @param modelVersion the model version
     * @return the TextAnalyticsRequestOptions object itself
     */
    public TextAnalyticsRequestOptions setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
        return this;
    }

    /**
     * Get the value of {@code showStatistics}
     *
     * @return the value of {@code showStatistics}
     */
    public boolean showStatistics() {
        return showStatistics;
    }

    /**
     * Set the value of {@code showStatistics}
     *
     * @param showStatistics if a boolean value was specified in the request this field will contain
     * information about the document payload
     * @return the TextAnalyticsRequestOptions object itself
     */
    public TextAnalyticsRequestOptions setShowStatistics(boolean showStatistics) {
        this.showStatistics = showStatistics;
        return this;
    }
}
