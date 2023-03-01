// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Fluent;

/**
 * Configurations that allow callers to specify details about how to execute an abstractive summarization in a
 * set of documents.
 */
@Fluent
public final class AbstractSummaryOptions extends TextAnalyticsRequestOptions {
    private String autoDetectionDefaultLanguage;
    private String displayName;
    private Integer maxSentenceCount;

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
     * @return The {@link AbstractSummaryOptions} object itself.
     */
    public AbstractSummaryOptions setAutoDetectionDefaultLanguage(String language) {
        autoDetectionDefaultLanguage = language;
        return this;
    }

    /**
     * Gets display name of the operation.
     *
     * @return Display name of the operation.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets display name of the operation.
     *
     * @param displayName Display name of the operation.
     *
     * @return The {@link AbstractSummaryOptions} object itself.
     */
    public AbstractSummaryOptions setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    /**
     * Get the maxSentenceCount property: It controls the approximate number of sentences in the output summaries.
     *
     * @return the maxSentenceCount value.
     */
    public Integer getMaxSentenceCount() {
        return this.maxSentenceCount;
    }

    /**
     * Set the maxSentenceCount property: It controls the approximate number of sentences in the output summaries.
     *
     * @param maxSentenceCount the maxSentenceCount value to set.
     * @return the AbstractSummaryOptions object itself.
     */
    public AbstractSummaryOptions setMaxSentenceCount(Integer maxSentenceCount) {
        this.maxSentenceCount = maxSentenceCount;
        return this;
    }
}
