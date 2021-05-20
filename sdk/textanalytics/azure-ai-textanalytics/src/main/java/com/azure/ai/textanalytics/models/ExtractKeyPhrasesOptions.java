// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Fluent;

/**
 * The {@link ExtractKeyPhrasesOptions} model.
 */
@Fluent
public final class ExtractKeyPhrasesOptions extends TextAnalyticsRequestOptions {
    /**
     * Set the model version. This value indicates which model will be used for scoring, e.g. "latest", "2019-10-01".
     * If a model-version is not specified, the API will default to the latest, non-preview version.
     *
     * @param modelVersion The model version.
     *
     * @return The {@link ExtractKeyPhrasesOptions} object itself.
     */
    @Override
    public ExtractKeyPhrasesOptions setModelVersion(String modelVersion) {
        super.setModelVersion(modelVersion);
        return this;
    }

    /**
     * Set the value of {@code includeStatistics}. The default value is false by default.
     * If set to true, indicates that the service should return document and document batch statistics
     * with the results of the operation.
     *
     * @param includeStatistics If a boolean value was specified in the request this field will contain
     * information about the document payload.
     *
     * @return the {@link ExtractKeyPhrasesOptions} object itself.
     */
    @Override
    public ExtractKeyPhrasesOptions setIncludeStatistics(boolean includeStatistics) {
        super.setIncludeStatistics(includeStatistics);
        return this;
    }

    /**
     * Set the value of {@code disableServiceLogs}.
     *
     * @param disableServiceLogs The default value of this property is 'false', except for methods like
     * 'beginAnalyzeHealthcareEntities' and 'recognizePiiEntities'. This means, Text Analytics service logs
     * your input text for 48 hours, solely to allow for troubleshooting issues. Setting this property to true,
     * disables input logging and may limit our ability to investigate issues that occur.
     *
     * @return the {@link ExtractKeyPhrasesOptions} object itself.
     */
    @Override
    public ExtractKeyPhrasesOptions setServiceLogsDisabled(boolean disableServiceLogs) {
        super.setServiceLogsDisabled(disableServiceLogs);
        return this;
    }
}
