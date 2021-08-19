// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Fluent;

/**
 * The {@link AnalyzeSentimentOptions} model.
 */
@Fluent
public final class AnalyzeSentimentOptions extends TextAnalyticsRequestOptions {

    /*
     * The boolean indicator to include opinion mining data in the returned result. If this
     * flag is specified, you'll get a {@code opinions} property on SentenceSentiment. It is provided by service
     * v3.1 and later.
     */
    private boolean includeOpinionMining;

    /**
     * Sets the model version. This value indicates which model will be used for scoring, e.g. "latest", "2019-10-01".
     * If a model-version is not specified, the API will default to the latest, non-preview version.
     *
     * @param modelVersion The model version.
     *
     * @return The {@link AnalyzeSentimentOptions} object itself.
     */
    @Override
    public AnalyzeSentimentOptions setModelVersion(String modelVersion) {
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
     * @return The {@link AnalyzeSentimentOptions} object itself.
     */
    @Override
    public AnalyzeSentimentOptions setIncludeStatistics(boolean includeStatistics) {
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
     * @return The {@link AnalyzeSentimentOptions} object itself.
     */
    @Override
    public AnalyzeSentimentOptions setServiceLogsDisabled(boolean disableServiceLogs) {
        super.setServiceLogsDisabled(disableServiceLogs);
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
     * @return The AnalyzeSentimentOptions object itself.
     */
    public AnalyzeSentimentOptions setIncludeOpinionMining(boolean includeOpinionMining) {
        this.includeOpinionMining = includeOpinionMining;
        return this;
    }
}
