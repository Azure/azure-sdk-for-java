// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Fluent;

/**
 * The {@link AnalyzeHealthcareEntitiesOptions} model.
 */
@Fluent
public final class AnalyzeHealthcareEntitiesOptions extends TextAnalyticsRequestOptions {
    private String autoDetectionDefaultLanguage;
    private String displayName;
    private Boolean disableServiceLogs;
    private FhirVersion fhirVersion;
    private HealthcareDocumentType documentType;

    /**
     * Gets auto detection fallback language code.
     *
     * @return Auto detection fallback language code.
     */
    public String getAutoDetectionDefaultLanguage() {
        return autoDetectionDefaultLanguage;
    }

    /**
     * Sets auto detection fallback language code.
     *
     * @param language Auto detection fallback language code.
     *
     * @return The {@link AnalyzeHealthcareEntitiesOptions} object itself.
     */
    public AnalyzeHealthcareEntitiesOptions setAutoDetectionDefaultLanguage(String language) {
        autoDetectionDefaultLanguage = language;
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
     * @return The {@link AnalyzeHealthcareEntitiesOptions} object itself.
     */
    public AnalyzeHealthcareEntitiesOptions setDisplayName(String displayName) {
        this.displayName = displayName;
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
     * @return the AnalyzeHealthcareEntitiesOptions object itself.
     */
    public AnalyzeHealthcareEntitiesOptions setFhirVersion(FhirVersion fhirVersion) {
        this.fhirVersion = fhirVersion;
        return this;
    }

    /**
     * Get the documentType property: Document type that can be provided as input for Fhir Documents. Expect to have
     * fhirVersion provided when used. Behavior of using None enum is the same as not using the documentType parameter.
     *
     * @return the documentType value.
     */
    public HealthcareDocumentType getDocumentType() {
        return this.documentType;
    }

    /**
     * Set the documentType property: Document type that can be provided as input for Fhir Documents. Expect to have
     * fhirVersion provided when used. Behavior of using None enum is the same as not using the documentType parameter.
     *
     * @param documentType the documentType value to set.
     * @return The {@link AnalyzeHealthcareEntitiesOptions} object itself.
     */
    public AnalyzeHealthcareEntitiesOptions setDocumentType(HealthcareDocumentType documentType) {
        this.documentType = documentType;
        return this;
    }

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
     * @return true if service logging of input text is disabled.
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
