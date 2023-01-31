// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Fluent;

/**
 * Configurations that allow callers to specify details about how to execute an extractive summarization in a
 * set of documents.
 */
@Fluent
public final class ExtractSummaryOptions extends TextAnalyticsRequestOptions {
    private String displayName;
    private Integer maxSentenceCount;
    private SummarySentencesOrder orderBy;

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
     * @return The {@link ExtractSummaryOptions} object itself.
     */
    public ExtractSummaryOptions setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    /**
     * Gets the maximum extractive summarization sentences number to be returned in the response.
     * If 'null' or not specified, a default value of 3 will be used as the maximum sentences number in the service
     * side.
     *
     * @return The maximum extractive summarization sentences number to be returned in the response.
     */
    public Integer getMaxSentenceCount() {
        return this.maxSentenceCount;
    }

    /**
     * Sets the maximum extractive summarization sentences number to be returned in the response.
     * If 'null' or not specified, a default value of 3 will be used as the maximum sentences number in the service
     * side.
     *
     * @param maxSentenceCount The maximum extractive summarization sentences number to be returned in the response.
     *
     * @return The {@link ExtractSummaryOptions} object itself.
     */
    public ExtractSummaryOptions setMaxSentenceCount(Integer maxSentenceCount) {
        this.maxSentenceCount = maxSentenceCount;
        return this;
    }

    /**
     * Gets the order in which the summary sentences will be presented by.
     *
     * @return The order in which the summary sentences will be presented by.
     */
    public SummarySentencesOrder getOrderBy() {
        return orderBy;
    }

    /**
     * Sets the order in which the summary sentences will be presented by.
     * Defaults to {@link SummarySentencesOrder#OFFSET} if not specified.
     *
     * @param orderBy The type of summary sentences order. Defaults to {@link SummarySentencesOrder#OFFSET}
     * if not specified.
     *
     * @return The {@link ExtractSummaryOptions} object itself.
     */
    public ExtractSummaryOptions setOrderBy(SummarySentencesOrder orderBy) {
        this.orderBy = orderBy;
        return this;
    }
}
