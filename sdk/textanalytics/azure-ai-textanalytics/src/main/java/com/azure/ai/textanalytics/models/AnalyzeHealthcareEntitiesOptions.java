// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Fluent;

/**
 * The {@link AnalyzeHealthcareEntitiesOptions} model.
 */
@Fluent
public final class AnalyzeHealthcareEntitiesOptions extends TextAnalyticsRequestOptions {
    private Boolean disableServiceLogs;

    /**
     * Sets the model version. This value indicates which model will be used for scoring, e.g. "latest", "2019-10-01".
     * If a model-version is not specified, the API will default to the latest, non-preview version.
     *
     * @param modelVersion The model version.
     *
     * @return The {@link AnalyzeHealthcareEntitiesOptions} object itself.
     */
    @Override
    public AnalyzeHealthcareEntitiesOptions setModelVersion(String modelVersion) {
        super.setModelVersion(modelVersion);
        return this;
    }

    /**
     * Sets the value of {@code includeStatistics}.
     *
     * @param includeStatistics If a boolean value was specified in the request this field will contain
     * information about the document payload.
     *
     * @return The {@link AnalyzeHealthcareEntitiesOptions} object itself.
     */
    @Override
    public AnalyzeHealthcareEntitiesOptions setIncludeStatistics(boolean includeStatistics) {
        super.setIncludeStatistics(includeStatistics);
        return this;
    }

    /**
     * Gets the value of service logs disable status. The default value of this property is 'true'. This means,
     * Text Analytics service won't log your input text. Setting this property to 'false', enables logging your input
     * text for 48 hours, solely to allow for troubleshooting issues.
     *
     * @return true if logging input text service are disabled.
     */
    @Override
    public boolean isServiceLogsDisabled() {
        return disableServiceLogs == null ? true : disableServiceLogs;
    }

    /**
     * Sets the value of service logs disable status.
     *
     * @param disableServiceLogs The default value of this property is 'true'. This means, Text Analytics service
     * does not log your input text. Setting this property to 'false', enables the service to log your text input for
     * 48 hours, solely to allow for troubleshooting issues.
     *
     * @return The {@link AnalyzeHealthcareEntitiesOptions} object itself.
     */
    @Override
    public AnalyzeHealthcareEntitiesOptions setServiceLogsDisabled(boolean disableServiceLogs) {
        this.disableServiceLogs = disableServiceLogs;
        return this;
    }
}
