// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Fluent;

/** The PiiEntitiesRecognition model. */
@Fluent
public final class PiiEntitiesRecognition {
    private PiiEntityDomainType domainFilter;
    // TODO: currently, service does not set their default values for model version, we temporally set the default
    // value to 'latest' until service correct it. https://github.com/Azure/azure-sdk-for-java/issues/17625
    private String modelVersion = "latest";

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
     * @return The RecognizePiiEntityOptions object itself.
     */
    public PiiEntitiesRecognition setDomainFilter(PiiEntityDomainType domainFilter) {
        this.domainFilter = domainFilter;
        return this;
    }

    /**
     * Get the modelVersion property: The model-version property.
     *
     * @return the modelVersion value.
     */
    public String getModelVersion() {
        return this.modelVersion;
    }

    /**
     * Set the modelVersion property: The model-version property.
     *
     * @param modelVersion the modelVersion value to set.
     * @return the PiiEntitiesRecognition object itself.
     */
    public PiiEntitiesRecognition setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
        return this;
    }
}
