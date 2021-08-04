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
    private String modelVersion;
    private boolean disableServiceLogs;
    private PiiEntityDomain domainFilter;
    private Iterable<PiiEntityCategory> categoriesFilter;

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
     * Gets the value of {@code disableServiceLogs}.
     *
     * @return The value of {@code disableServiceLogs}. The default value of this property is 'false'. This means,
     * Text Analytics service logs your input text for 48 hours, solely to allow for troubleshooting issues. Setting
     * this property to true, disables input logging and may limit our ability to investigate issues that occur.
     */
    public boolean isServiceLogsDisabled() {
        return disableServiceLogs;
    }

    /**
     * Sets the value of {@code disableServiceLogs}.
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
     * See https://aka.ms/tanerpii for more information.
     *
     * @return The value of domainFilter.
     */
    public PiiEntityDomain getDomainFilter() {
        return domainFilter;
    }

    /**
     * Sets the value of domainFilter. It filters the response entities to ones only included in the specified domain.
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
