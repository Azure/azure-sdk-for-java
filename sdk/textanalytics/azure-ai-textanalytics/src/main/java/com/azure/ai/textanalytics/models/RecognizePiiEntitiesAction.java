// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Fluent;

import java.util.Arrays;

/**
 * The option bags to use when recognizing Personally Identifiable Information(PII) entities as an action.
 */
@Fluent
public final class RecognizePiiEntitiesAction {
    private String actionName;
    private String modelVersion;
    private boolean includeStatistics;
    private boolean disableServiceLogs;
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
     * Set the model version. This value indicates which model will be used for scoring, e.g. "latest", "2019-10-01".
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
     * Get the value of {@code includeStatistics}.
     *
     * @return The value of {@code includeStatistics}.
     */
    public boolean isIncludeStatistics() {
        return includeStatistics;
    }

    /**
     * Set the value of {@code includeStatistics}. The default value is false by default.
     * If set to true, indicates that the service should return document and document batch statistics
     * with the results of the operation.
     *
     * @param includeStatistics If a boolean value was specified in the request this field will contain
     * information about the document payload.
     *
     * @return The {@link RecognizePiiEntitiesAction} object itself.
     */
    public RecognizePiiEntitiesAction setIncludeStatistics(boolean includeStatistics) {
        this.includeStatistics = includeStatistics;
        return this;
    }

    /**
     * Get the value of {@code disableServiceLogs}.
     *
     * @return The value of {@code disableServiceLogs}. The default value of this property is 'false'. This means,
     * Text Analytics service logs your input text for 48 hours, solely to allow for troubleshooting issues. Setting
     * this property to true, disables input logging and may limit our ability to investigate issues that occur.
     */
    public boolean isServiceLogsDisabled() {
        return disableServiceLogs;
    }

    /**
     * Set the value of {@code disableServiceLogs}.
     *
     * @param disableServiceLogs The default value of this property is 'false', except for methods like
     * 'beginAnalyzeHealthcareEntities' and 'recognizePiiEntities'. This means, Text Analytics service logs
     * your input text for 48 hours, solely to allow for troubleshooting issues. Setting this property to true,
     * disables input logging and may limit our ability to investigate issues that occur.
     *
     * @return The {@link RecognizePiiEntitiesAction} object itself.
     */
    public RecognizePiiEntitiesAction setServiceLogsDisabled(boolean disableServiceLogs) {
        this.disableServiceLogs = disableServiceLogs;
        return this;
    }

    /**
     * Get the value of domainFilter. It filters the response entities to ones only included in the specified domain.
     * I.e., if set to 'PHI', will only return entities in the Protected Healthcare Information domain.
     * See https://aka.ms/tanerpii for more information.
     *
     * @return The value of domainFilter.
     */
    public PiiEntityDomain getDomainFilter() {
        return domainFilter;
    }

    /**
     * Set the value of domainFilter. It filters the response entities to ones only included in the specified domain.
     * I.e., if set to 'PHI', will only return entities in the Protected Healthcare Information domain.
     * See https://aka.ms/tanerpii for more information.
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
     * Get the value of categoriesFilter. It filters the response entities to ones only included in the specified
     * categories.
     *
     * @return The value of categoriesFilter.
     */
    public Iterable<PiiEntityCategory> getCategoriesFilter() {
        return categoriesFilter;
    }

    /**
     * Set the value of categoriesFilter. It filters the response entities to ones only included in the specified
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
