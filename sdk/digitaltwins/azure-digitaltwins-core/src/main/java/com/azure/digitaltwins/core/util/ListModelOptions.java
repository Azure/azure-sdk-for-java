package com.azure.digitaltwins.core.util;

import com.azure.core.annotation.Fluent;

import java.util.List;

@Fluent
public final class ListModelOptions {

    /*
     * The maximum number of items to retrieve per request. The server may
     * choose to return less than the requested max.
     */
    private Integer maxItemCount;

    /**
     * Get the maxItemCount property.
     *
     * @return the maxItemCount value.
     */
    public Integer getMaxItemCount() {
        return this.maxItemCount;
    }

    /**
     * Set the maxItemCount property.
     *
     * @param maxItemCount the maxItemCount value to set.
     * @return the ListModelOptions object itself.
     */
    public ListModelOptions setMaxItemCount(Integer maxItemCount) {
        this.maxItemCount = maxItemCount;
        return this;
    }

    /*
     * Whether to include the model definition in the result. If false, only the model metadata will be returned.
     */
    private Boolean includeModelDefinition;

    /**
     * Get the includeModelDefinition property.
     *
     * @return the includeModelDefinition value.
     */
    public Boolean getIncludeModelDefinition() { return this.includeModelDefinition; }

    /**
     * Set the includeModelDefinition property.
     *
     * @param includeModelDefinition the includeModelDefinition value to set.
     * @return the ListModelOptions object itself.
     */
    public ListModelOptions setIncludeModelDefinition(Boolean includeModelDefinition) {
        this.includeModelDefinition = includeModelDefinition;
        return this;
    }

    /*
    * The model Ids to have dependencies retrieved.
    */
    private List<String> dependenciesFor;

    /**
     * Get the dependenciesFor property.
     *
     * @return the dependenciesFor value.
     */
    public List<String> getDependenciesFor() { return this.dependenciesFor; }

    /**
     * Set the dependenciesFor property.
     *
     * @param dependenciesFor the dependenciesFor value to set.
     * @return the ListModelOptions object itself.
     */
    public ListModelOptions setDependenciesFor(List<String> dependenciesFor) {
        this.dependenciesFor = dependenciesFor;
        return this;
    }
}
