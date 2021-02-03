// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Fluent;

/**
 * The {@link RecognizePiiEntitiesOptions} model.
 */
@Fluent
public final class RecognizePiiEntitiesOptions extends TextAnalyticsRequestOptions {
    private PiiEntityDomainType domainFilter;

    /**
     * Set the model version. This value indicates which model will be used for scoring, e.g. "latest", "2019-10-01".
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
     * Set the value of {@code includeStatistics}.
     *
     * @param includeStatistics If a boolean value was specified in the request this field will contain
     * information about the document payload.
     *
     * @return the {@link RecognizePiiEntitiesOptions} object itself.
     */
    @Override
    public RecognizePiiEntitiesOptions setIncludeStatistics(boolean includeStatistics) {
        super.setIncludeStatistics(includeStatistics);
        return this;
    }

    /**
     * Get the value of domainFilter. It filters the response entities to ones only included in the specified domain.
     * I.e., if set to 'PHI', will only return entities in the Protected Healthcare Information domain.
     * See https://aka.ms/tanerpii for more information.
     *
     * @return The value of domainFilter.
     */
    public PiiEntityDomainType getDomainFilter() {
        return domainFilter;
    }

    /**
     * Set the value of domainFilter. It filters the response entities to ones only included in the specified domain.
     * I.e., if set to 'PHI', will only return entities in the Protected Healthcare Information domain.
     * See https://aka.ms/tanerpii for more information.
     *
     * @param domainFilter It filters the response entities to ones only included in the specified domain.
     *
     * @return The {@link RecognizePiiEntitiesOptions} object itself.
     */
    public RecognizePiiEntitiesOptions setDomainFilter(PiiEntityDomainType domainFilter) {
        this.domainFilter = domainFilter;
        return this;
    }
}
