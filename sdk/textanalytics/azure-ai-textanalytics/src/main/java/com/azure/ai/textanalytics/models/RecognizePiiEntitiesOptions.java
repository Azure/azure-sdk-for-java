// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Fluent;

import java.util.Arrays;

/**
 * The {@link RecognizePiiEntitiesOptions} model.
 */
@Fluent
public final class RecognizePiiEntitiesOptions extends TextAnalyticsRequestOptions {
    private Boolean disableServiceLogs;
    private PiiEntityDomain domainFilter;
    private Iterable<PiiEntityCategory> categoriesFilter;

    /**
     * Sets the model version. This value indicates which model will be used for scoring, e.g. "latest", "2019-10-01".
     * If a model-version is not specified, the API will default to the latest, non-preview version.
     *
     * @param modelVersion The model version.
     *
     * @return The {@link RecognizePiiEntitiesOptions} object itself.
     */
    @Override
    public RecognizePiiEntitiesOptions setModelVersion(String modelVersion) {
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
     * @return The {@link RecognizePiiEntitiesOptions} object itself.
     */
    @Override
    public RecognizePiiEntitiesOptions setIncludeStatistics(boolean includeStatistics) {
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
     * @return The {@link RecognizePiiEntitiesOptions} object itself.
     */
    @Override
    public RecognizePiiEntitiesOptions setServiceLogsDisabled(boolean disableServiceLogs) {
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
     * @return The {@link RecognizePiiEntitiesOptions} object itself.
     */
    public RecognizePiiEntitiesOptions setDomainFilter(PiiEntityDomain domainFilter) {
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
     * @return The {@link RecognizePiiEntitiesOptions} object itself.
     */
    public RecognizePiiEntitiesOptions setCategoriesFilter(PiiEntityCategory... categoriesFilter) {
        this.categoriesFilter = Arrays.asList(categoriesFilter);
        return this;
    }
}
