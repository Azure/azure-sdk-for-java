// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Fluent;

/**
 * Configurations that allow callers to specify details about how to execute an healthcare analysis action in a set of
 * documents.
 */
@Fluent
public class AnalyzeHealthcareEntitiesAction {
    private String actionName;
    private String modelVersion;
    private Boolean disableServiceLogs;
    private FhirVersion fhirVersion;

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
     * @return The {@link AnalyzeHealthcareEntitiesAction} object itself.
     */
    public AnalyzeHealthcareEntitiesAction setActionName(String actionName) {
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
     * @return The {@link AnalyzeHealthcareEntitiesAction} object itself.
     */
    public AnalyzeHealthcareEntitiesAction setModelVersion(String modelVersion) {
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
     * @return The {@link AnalyzeHealthcareEntitiesAction} object itself.
     */
    public AnalyzeHealthcareEntitiesAction setServiceLogsDisabled(boolean disableServiceLogs) {
        this.disableServiceLogs = disableServiceLogs;
        return this;
    }

    /**
     * Get the fhirVersion property: The FHIR Spec version that the result will use to format the fhirBundle. For
     * additional information see https://www.hl7.org/fhir/overview.html.
     *
     * @return the fhirVersion value.
     */
    public FhirVersion getFhirVersion() {
        return fhirVersion;
    }

    /**
     * Set the fhirVersion property: The FHIR Spec version that the result will use to format the fhirBundle. For
     * additional information see https://www.hl7.org/fhir/overview.html.
     *
     * @param fhirVersion the fhirVersion value to set.
     * @return the AnalyzeHealthcareEntitiesAction object itself.
     */
    public AnalyzeHealthcareEntitiesAction setFhirVersion(FhirVersion fhirVersion) {
        this.fhirVersion = fhirVersion;
        return this;
    }
}
