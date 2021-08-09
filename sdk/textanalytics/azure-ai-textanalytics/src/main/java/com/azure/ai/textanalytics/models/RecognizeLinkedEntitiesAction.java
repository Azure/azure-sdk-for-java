// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Fluent;

/**
 * Configurations that allow callers to specify details about how to execute a linked entities recognition action in
 * a set of documents.
 */
@Fluent
public final class RecognizeLinkedEntitiesAction {
    private String modelVersion;
    private boolean disableServiceLogs;

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
     * @return The {@link RecognizeLinkedEntitiesAction} object itself.
     */
    public RecognizeLinkedEntitiesAction setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
        return this;
    }

    /**
     * Gets the value of service logs disable status. The default value of this property is 'false'. This means,
     * Text Analytics service logs your input text for 48 hours, solely to allow for troubleshooting issues. Setting
     * this property to true, disables input logging and may limit our ability to investigate issues that occur.
     *
     * @return true if logging input text service are disabled.
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
     * @return The {@link RecognizeLinkedEntitiesAction} object itself.
     */
    public RecognizeLinkedEntitiesAction setServiceLogsDisabled(boolean disableServiceLogs) {
        this.disableServiceLogs = disableServiceLogs;
        return this;
    }
}
