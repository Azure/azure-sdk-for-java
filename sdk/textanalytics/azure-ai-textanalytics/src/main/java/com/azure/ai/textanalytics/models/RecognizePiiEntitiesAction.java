// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Fluent;

import java.util.Arrays;

/**
 * Configurations that allow callers to specify details about how to execute a Personally Identifiable Information(PII)
 * entities recognition action in a set of documents.
 */
@Fluent
public final class RecognizePiiEntitiesAction {
    private String actionName;
    private String modelVersion;
    private Boolean disableServiceLogs;
    private PiiEntityDomain domainFilter;
    private Iterable<PiiEntityCategory> categoriesFilter;

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
     * @return The {@link RecognizePiiEntitiesAction} object itself.
     */
    public RecognizePiiEntitiesAction setActionName(String actionName) {
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
     * @return The {@link RecognizePiiEntitiesAction} object itself.
     */
    public RecognizePiiEntitiesAction setModelVersion(String modelVersion) {
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
     * @return The {@link RecognizePiiEntitiesAction} object itself.
     */
    public RecognizePiiEntitiesAction setServiceLogsDisabled(boolean disableServiceLogs) {
        this.disableServiceLogs = disableServiceLogs;
        return this;
    }

    /**
     * Gets the value of domainFilter. It filters the response entities to ones only included in the specified domain.
     * I.e., if set to 'PHI', will only return entities in the Protected Healthcare Information domain.
     * See https://aka.ms/azsdk/language/pii for more information.
     *
     * @return The value of domainFilter.
     */
    public PiiEntityDomain getDomainFilter() {
        return domainFilter;
    }

    /**
     * Sets the value of domainFilter. It filters the response entities to ones only included in the specified domain.
     * I.e., if set to 'PHI', will only return entities in the Protected Healthcare Information domain.
     * See https://aka.ms/azsdk/language/pii for more information.
     *
     * @param domainFilter It filters the response entities to ones only included in the specified domain.
     *
     * @return The {@link RecognizePiiEntitiesAction} object itself.
     */
    public RecognizePiiEntitiesAction setDomainFilter(PiiEntityDomain domainFilter) {
        this.domainFilter = domainFilter;
        return this;
    }

    /**
     * Gets the value of categoriesFilter. It filters the response entities to ones only included in the specified
     * categories.
     *
     * @return The value of categoriesFilter.
     */
    public Iterable<PiiEntityCategory> getCategoriesFilter() {
        return categoriesFilter;
    }

    /**
     * Sets the value of categoriesFilter. It filters the response entities to ones only included in the specified
     * categories.
     *
     * @param categoriesFilter It filters the response entities to ones only included in the specified categories.
     *
     * @return The {@link RecognizePiiEntitiesAction} object itself.
     */
    public RecognizePiiEntitiesAction setCategoriesFilter(PiiEntityCategory... categoriesFilter) {
        this.categoriesFilter = Arrays.asList(categoriesFilter);
        return this;
    }
}
