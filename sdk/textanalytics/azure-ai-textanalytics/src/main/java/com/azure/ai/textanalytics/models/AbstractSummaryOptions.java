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
     * Sets the model version. This value indicates which model will be used for scoring, e.g. "latest", "2019-10-01".
     * If a model-version is not specified, the API will default to the latest, non-preview version.
     *
     * @param modelVersion The model version.
     *
     * @return The {@link AbstractSummaryOptions} object itself.
     */
    @Override
    public AbstractSummaryOptions setModelVersion(String modelVersion) {
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
     * @return The {@link AbstractSummaryOptions} object itself.
     */
    @Override
    public AbstractSummaryOptions setIncludeStatistics(boolean includeStatistics) {
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
     * @return The {@link AbstractSummaryOptions} object itself.
     */
    @Override
    public AbstractSummaryOptions setServiceLogsDisabled(boolean disableServiceLogs) {
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
