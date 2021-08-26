// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.Context;
import com.azure.digitaltwins.core.implementation.models.DigitalTwinModelsListOptions;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

// This class manually copies the generated class of the same name, but also adds properties for includeModelDefinition
// and dependenciesFor since the swagger does not group those in with these options for us.

/**
 * The optional parameters for
 * {@link com.azure.digitaltwins.core.DigitalTwinsClient#listModels(ListModelsOptions, Context)} and
 * {@link com.azure.digitaltwins.core.DigitalTwinsAsyncClient#listModels(ListModelsOptions)}
 */
@Fluent
public final class ListModelsOptions {
    /*
     * The maximum number of items to retrieve per request. The server may
     * choose to return less than the requested number.
     */
    @JsonProperty(value = "MaxItemsPerPage")
    private Integer maxItemsPerPage;

    /**
     * Get the maxItemsPerPage property: The maximum number of items to retrieve per request. The server may choose to
     * return less than the requested number.
     *
     * @return the maxItemsPerPage value.
     */
    public Integer getMaxItemsPerPage() {
        return this.maxItemsPerPage;
    }

    /**
     * Set the maxItemsPerPage property: The maximum number of items to retrieve per request. The server may choose to
     * return less than the requested number.
     *
     * @param maxItemsPerPage the maxItemsPerPage value to set.
     * @return the ListModelsOptions object itself.
     */
    public ListModelsOptions setMaxItemsPerPage(Integer maxItemsPerPage) {
        this.maxItemsPerPage = maxItemsPerPage;
        return this;
    }

    /*
     * Whether to include the model definition in the result. If false, only the model metadata will be returned.
     * Disabled by default.
     */
    private Boolean includeModelDefinition = false;

    /**
     * Gets whether to include the model definition in the result. If false, only the model metadata will be returned.
     * @return the includeModelDefinition value.
     */
    public Boolean getIncludeModelDefinition() { return this.includeModelDefinition; }

    /**
     * Sets whether to include the model definition in the result. If false, only the model metadata will be returned.
     * @param includeModelDefinition the includeModelDefinition value to set.
     * @return the ListModelOptions object itself.
     */
    public ListModelsOptions setIncludeModelDefinition(Boolean includeModelDefinition) {
        this.includeModelDefinition = includeModelDefinition;
        return this;
    }

    /*
     * The model Ids to have dependencies retrieved.
     */
    private List<String> dependenciesFor;

    /**
     * Gets the model Ids that will have their dependencies retrieved.
     * @return the dependenciesFor value.
     */
    public List<String> getDependenciesFor() { return this.dependenciesFor; }

    /**
     * Sets the model Ids that will have their dependencies retrieved.
     * @param dependenciesFor the dependenciesFor value to set.
     * @return the ListModelOptions object itself.
     */
    public ListModelsOptions setDependenciesFor(List<String> dependenciesFor) {
        this.dependenciesFor = dependenciesFor;
        return this;
    }
}
