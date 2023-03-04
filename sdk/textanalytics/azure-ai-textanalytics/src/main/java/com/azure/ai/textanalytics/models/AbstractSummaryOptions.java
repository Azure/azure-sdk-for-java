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
    private String displayName;
    private Integer sentenceCount;

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
     * Get the sentenceCount property: It controls the approximate number of sentences in the output summaries.
     *
     * @return the sentenceCount value.
     */
    public Integer getSentenceCount() {
        return this.sentenceCount;
    }

    /**
     * Set the sentenceCount property: It controls the approximate number of sentences in the output summaries.
     *
     * @param sentenceCount the sentenceCount value to set.
     * @return the AbstractSummaryOptions object itself.
     */
    public AbstractSummaryOptions setSentenceCount(Integer sentenceCount) {
        this.sentenceCount = sentenceCount;
        return this;
    }
}
