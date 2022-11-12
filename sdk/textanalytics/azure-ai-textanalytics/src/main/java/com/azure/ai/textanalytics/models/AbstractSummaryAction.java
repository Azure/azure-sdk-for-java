// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Fluent;

/**
 * Configurations that allow callers to specify details about how to execute an abstractive summarization action in a
 * set of documents.
 */
@Fluent
public final class AbstractSummaryAction {
    private String actionName;
    private String modelVersion;
    private Boolean disableServiceLogs;
    private Integer maxSentenceCount;

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
     * @return The {@link AbstractSummaryAction} object itself.
     */
    public AbstractSummaryAction setActionName(String actionName) {
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
     * @return The {@link AbstractSummaryAction} object itself.
     */
    public AbstractSummaryAction setModelVersion(String modelVersion) {
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
     * @return The {@link AbstractSummaryAction} object itself.
     */
    public AbstractSummaryAction setServiceLogsDisabled(boolean disableServiceLogs) {
        this.disableServiceLogs = disableServiceLogs;
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
     * @return the AbstractSummaryAction object itself.
     */
    public AbstractSummaryAction setMaxSentenceCount(Integer maxSentenceCount) {
        this.maxSentenceCount = maxSentenceCount;
        return this;
    }
}
