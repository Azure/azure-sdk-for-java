package com.azure.digitaltwins.core.models;

import com.azure.core.annotation.Fluent;

import java.util.List;

/**
 * The optional parameters when listing models.
 */
@Fluent
public final class ModelsListOptions {

    /*
     * The maximum number of items to retrieve per request. The server may
     * choose to return less than the requested max.
     */
    private Integer maxItemCount;

    /**
     * Gets the maximum number of items to retrieve per request. The server may choose to return less than the
     * requested max.
     * @return the maxItemCount value.
     */
    public Integer getMaxItemCount() {
        return this.maxItemCount;
    }

    /**
     * Sets the maximum number of items to retrieve per request. The server may choose to return less than the
     * requested max.
     * @param maxItemCount the maxItemCount value to set.
     * @return the ModelsListOptions object itself.
     */
    public ModelsListOptions setMaxItemCount(Integer maxItemCount) {
        this.maxItemCount = maxItemCount;
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
     * @return the ModelsListOptions object itself.
     */
    public ModelsListOptions setIncludeModelDefinition(Boolean includeModelDefinition) {
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
     * @return the ModelsListOptions object itself.
     */
    public ModelsListOptions setDependenciesFor(List<String> dependenciesFor) {
        this.dependenciesFor = dependenciesFor;
        return this;
    }
}
