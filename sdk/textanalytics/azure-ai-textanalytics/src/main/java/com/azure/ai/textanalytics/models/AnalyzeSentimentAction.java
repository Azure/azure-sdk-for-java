// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Fluent;

/**
 * Configurations that allow callers to specify details about how to execute an sentiment analysis action in a set of
 * documents.
 */
@Fluent
public final class AnalyzeSentimentAction {
    private String modelVersion;
    private boolean disableServiceLogs;
    private boolean includeOpinionMining;

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
     * @return The {@link AnalyzeSentimentAction} object itself.
     */
    public AnalyzeSentimentAction setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
        return this;
    }

    /**
     * Gets the value of service logs disable status. The default value of this property is 'false'. This means,
     * Text Analytics service logs your input text for 48 hours, solely to allow for troubleshooting issues. Setting
     * this property to true, disables input logging and may limit our ability to investigate issues that occur.
     *
     * @return true if service logging of input text is disabled.
     */
    public boolean isServiceLogsDisabled() {
        return disableServiceLogs;
    }

    /**
     * Sets the value of service logs disable status.
     *
     * @param disableServiceLogs The default value of this property is 'false'. This means, Text Analytics service logs
     * your input text for 48 hours, solely to allow for troubleshooting issues. Setting this property to true,
     * disables input logging and may limit our ability to investigate issues that occur.
     *
     * @return The {@link AnalyzeSentimentAction} object itself.
     */
    public AnalyzeSentimentAction setServiceLogsDisabled(boolean disableServiceLogs) {
        this.disableServiceLogs = disableServiceLogs;
        return this;
    }

    /**
     * Gets the value of {@code includeOpinionMining}. The boolean indicator to include opinion mining data in the
     * returned result. If this flag is specified, you'll get a {@code opinions} property on SentenceSentiment.
     * It is provided by service v3.1 and later.
     *
     * @return The value of {@code includeOpinionMining}.
     */
    public boolean isIncludeOpinionMining() {
        return includeOpinionMining;
    }

    /**
     * Sets the value of {@code includeOpinionMining}. The boolean indicator to include opinion mining data in the
     * returned result. If this flag is specified, you'll get a {@code opinions} property on SentenceSentiment.
     * It is provided by service v3.1 and later.
     *
     * @param includeOpinionMining The boolean indicator to include opinion mining data in the returned result.
     *
     * @return The {@link AnalyzeSentimentAction} object itself.
     */
    public AnalyzeSentimentAction setIncludeOpinionMining(boolean includeOpinionMining) {
        this.includeOpinionMining = includeOpinionMining;
        return this;
    }
}
