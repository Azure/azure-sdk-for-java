// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Fluent;

/**
 * Configurations that allow callers to specify details about how to execute an extractive summarization action in a
 * set of documents.
 */
@Fluent
public final class ExtractiveSummaryAction {
    private String actionName;
    private String modelVersion;
    private Integer maxSentenceCount;
    private ExtractiveSummarySentencesOrder orderBy;
    private boolean disableServiceLogs;

    /**
     * Get the name of action.
     *
     * @return the name of action.
     */
    public String getActionName() {
        return actionName;
    }

    /**
     * Set the custom name for the action.
     *
     * @param actionName the custom name for the action.
     *
     * @return The {@link ExtractiveSummaryAction} object itself.
     */
    public ExtractiveSummaryAction setActionName(String actionName) {
        this.actionName = actionName;
        return this;
    }

    /**
     * Gets the version of the text analytics model used by this operation.
     *
     * @return The model version.
     */
    public String getModelVersion() {
        return modelVersion;
    }

    /**
     * Set the model version. This value indicates which model will be used for scoring, e.g. "latest", "2019-10-01".
     * If a model-version is not specified, the API will default to the latest, non-preview version.
     *
     * @param modelVersion The model version.
     *
     * @return The {@link ExtractiveSummaryAction} object itself.
     */
    public ExtractiveSummaryAction setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
        return this;
    }

    /**
     * Gets the value of {@code disableServiceLogs}.
     *
     * @return The value of {@code disableServiceLogs}. The default value of this property is 'false'. This means,
     * Text Analytics service logs your input text for 48 hours, solely to allow for troubleshooting issues. Setting
     * this property to true, disables input logging and may limit our ability to investigate issues that occur.
     */
    public boolean isServiceLogsDisabled() {
        return disableServiceLogs;
    }

    /**
     * Sets the value of {@code disableServiceLogs}.
     *
     * @param disableServiceLogs The default value of this property is 'false'. This means, Text Analytics service logs
     * your input text for 48 hours, solely to allow for troubleshooting issues. Setting this property to true,
     * disables input logging and may limit our ability to investigate issues that occur.
     *
     * @return The {@link ExtractiveSummaryAction} object itself.
     */
    public ExtractiveSummaryAction setServiceLogsDisabled(boolean disableServiceLogs) {
        this.disableServiceLogs = disableServiceLogs;
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
     * @return The {@link ExtractiveSummaryAction} object itself.
     */
    public ExtractiveSummaryAction setMaxSentenceCount(Integer maxSentenceCount) {
        this.maxSentenceCount = maxSentenceCount;
        return this;
    }

    /**
     * Gets the order in which the summary sentences will be presented by.
     *
     * @return The order in which the summary sentences will be presented by.
     */
    public ExtractiveSummarySentencesOrder getOrderBy() {
        return orderBy;
    }

    /**
     * Sets the order in which the summary sentences will be presented by.
     * Defaults to {@link ExtractiveSummarySentencesOrder#OFFSET} if not specified.
     *
     * @param orderBy The type of summary sentences order. Defaults to {@link ExtractiveSummarySentencesOrder#OFFSET}
     * if not specified.
     *
     * @return The {@link ExtractiveSummaryAction} object itself.
     */
    public ExtractiveSummaryAction setOrderBy(ExtractiveSummarySentencesOrder orderBy) {
        this.orderBy = orderBy;
        return this;
    }
}
