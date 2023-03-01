// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Fluent;

/**
 * The {@link AnalyzeActionsOptions} model.
 */
@Fluent
public final class AnalyzeActionsOptions {
    private String autoDetectionDefaultLanguage;
    private boolean includeStatistics;

    /**
     * Gets auto detection fallback language code.
     *
     * @return Auto detection fallback language code.
     */
    public String getAutoDetectionDefaultLanguage() {
        return autoDetectionDefaultLanguage;
    }

    /**
     * Sets auto detection fallback language code.
     *
     * @param language Auto detection fallback language code.
     *
     * @return The {@link AnalyzeActionsOptions} object itself.
     */
    public AnalyzeActionsOptions setAutoDetectionDefaultLanguage(String language) {
        autoDetectionDefaultLanguage = language;
        return this;
    }

    /**
     * Gets the value of {@code includeStatistics}.
     *
     * @return The value of {@code includeStatistics}.
     */
    public boolean isIncludeStatistics() {
        return includeStatistics;
    }

    /**
     * Sets the value of {@code includeStatistics}. The default value is false by default.
     * If set to true, indicates that the service should return document and document batch statistics
     * with the results of the operation.
     *
     * @param includeStatistics If a boolean value was specified in the request this field will contain
     * information about the document payload.
     *
     * @return the {@link AnalyzeActionsOptions} object itself.
     */
    public AnalyzeActionsOptions setIncludeStatistics(boolean includeStatistics) {
        this.includeStatistics = includeStatistics;
        return this;
    }
}
