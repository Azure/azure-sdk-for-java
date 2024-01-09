// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation.feature.entity;

import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.DEFAULT_REQUIREMENT_TYPE;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.azure.data.appconfiguration.models.FeatureFlagFilter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Azure App Configuration Feature Flag.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class Feature {

    @JsonProperty("key")
    private String key;

    @JsonProperty("evaluate")
    private Boolean evaluate = true;

    @JsonProperty("requirement-type")
    private String requirementType = DEFAULT_REQUIREMENT_TYPE;

    @JsonProperty("enabled-for")
    private Map<String, FeatureFlagFilter> enabledFor;

    @JsonProperty("allocation")
    private Allocation allocation;

    @JsonProperty("variants")
    private Map<String, VariantReference> variants;

    /**
     * Feature Flag object.
     */
    public Feature() {}

    /**
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * @param key the key to set
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * @return the evaluate
     */
    public Boolean getEvaluate() {
        return evaluate;
    }

    /**
     * @param evaluate the evaluate to set
     */
    public void setEvaluate(Boolean evaluate) {
        this.evaluate = evaluate;
    }

    /**
     * @return the requirementType
     */
    public String getRequirementType() {
        return requirementType;
    }

    /**
     * @param requirementType the requirementType to set
     */
    public void setRequirementType(String requirementType) {
        this.requirementType = requirementType;
    }

    /**
     * @return the allocation
     */
    public Allocation getAllocation() {
        return allocation;
    }

    /**
     * @param allocation the allocation to set
     */
    public void setAllocation(Allocation allocation) {
        this.allocation = allocation;
    }

    /**
     * @return the enabledFor
     */
    public Map<String, FeatureFlagFilter> getEnabledFor() {
        return enabledFor;
    }

    /**
     * @param featureFlagFilters the enabledFor to set
     */
    public void setEnabledFor(List<FeatureFlagFilter> featureFlagFilters) {
        enabledFor = new HashMap<>();
        for (int i = 0; i < featureFlagFilters.size(); i++) {
            enabledFor.put(String.valueOf(i), featureFlagFilters.get(i));
        }
    }

    /**
     * @return the variants
     */
    public Map<String, VariantReference> getVariants() {
        return variants;
    }

    /**
     * @param variants the variants to set
     */
    public void setVariants(List<VariantReference> variants) {
        this.variants = new HashMap<>();
        for (int i = 0; i < variants.size(); i++) {
            this.variants.put(String.valueOf(i), variants.get(i));
        }
    }

}
