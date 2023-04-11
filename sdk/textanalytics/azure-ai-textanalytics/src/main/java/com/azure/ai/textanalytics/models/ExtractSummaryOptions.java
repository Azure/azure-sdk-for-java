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
     * Sets the model version. This value indicates which model will be used for scoring, e.g. "latest", "2019-10-01".
     * If a model-version is not specified, the API will default to the latest, non-preview version.
     *
     * @param modelVersion The model version.
     *
     * @return The {@link ExtractSummaryOptions} object itself.
     */
    @Override
    public ExtractSummaryOptions setModelVersion(String modelVersion) {
        super.setModelVersion(modelVersion);
        return this;
    }

    /**
     * Sets the value of {@code includeStatistics}. The default value is false by default.
     * If set to true, indicates that the service should return document and document batch statistics
     * with the results of the operation.
     *
     * @param includeStatistics If a boolean value was specified in the request this field will contain
     * information about the document payload.
     *
     * @return The {@link ExtractSummaryOptions} object itself.
     */
    @Override
    public ExtractSummaryOptions setIncludeStatistics(boolean includeStatistics) {
        super.setIncludeStatistics(includeStatistics);
        return this;
    }

    /**
     * Sets the value of service logs disable status.
     *
     * @param disableServiceLogs The default value of this property is 'false', except for methods like
     * 'beginAnalyzeHealthcareEntities' and 'recognizePiiEntities'. This means, Text Analytics service logs
     * your input text for 48 hours, solely to allow for troubleshooting issues. Setting this property to true,
     * disables input logging and may limit our ability to investigate issues that occur.
     *
     * @return The {@link ExtractSummaryOptions} object itself.
     */
    @Override
    public ExtractSummaryOptions setServiceLogsDisabled(boolean disableServiceLogs) {
        super.setServiceLogsDisabled(disableServiceLogs);
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
