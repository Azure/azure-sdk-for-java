// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Fluent;

/**
 * Configurations that allow callers to specify details about how to execute an abstractive summarization action in a
 * set of documents.
 */
@Fluent
public final class AbstractiveSummaryAction {
    private String actionName;
    private String modelVersion;
    private Boolean disableServiceLogs;
    private Integer sentenceCount;

    /**
     * Constructs a {@code AbstractiveSummaryAction} model.
     */
    public AbstractiveSummaryAction() {
    }

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
     * @return The {@code AbstractiveSummaryAction} object itself.
     */
    public AbstractiveSummaryAction setActionName(String actionName) {
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
     * Sets the model version. This value indicates which model will be used for scoring, e.g. "latest", "2019-10-01".
     * If a model-version is not specified, the API will default to the latest, non-preview version.
     *
     * @param modelVersion The model version.
     *
     * @return The {@code AbstractiveSummaryAction} object itself.
     */
    public AbstractiveSummaryAction setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
        return this;
    }

    /**
     * Gets the value of service logs disable status. The default value of this property is 'true'. This means,
     * Text Analytics service won't log your input text. Setting this property to 'false', enables logging your input
     * text for 48 hours, solely to allow for troubleshooting issues.
     *
     * @return true if service logging of input text is disabled.
     */
    public boolean isServiceLogsDisabled() {
        return disableServiceLogs == null ? true : disableServiceLogs;
    }

    /**
     * Sets the value of service logs disable status.
     *
     * @param disableServiceLogs The default value of this property is 'true'. This means, Text Analytics service won't
     * log your input text. Setting this property to 'false', enables logging your input text for 48 hours,
     * solely to allow for troubleshooting issues.
     *
     * @return The {@code AbstractiveSummaryAction} object itself.
     */
    public AbstractiveSummaryAction setServiceLogsDisabled(boolean disableServiceLogs) {
        this.disableServiceLogs = disableServiceLogs;
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
     * @return the {@code AbstractiveSummaryAction}  object itself.
     */
    public AbstractiveSummaryAction setSentenceCount(Integer sentenceCount) {
        this.sentenceCount = sentenceCount;
        return this;
    }
}
