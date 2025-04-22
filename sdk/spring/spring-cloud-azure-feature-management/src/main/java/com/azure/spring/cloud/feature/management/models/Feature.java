// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.feature.management.models;

import java.util.List;

import org.springframework.lang.NonNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * App Configuration Feature defines the feature name and a Map of FeatureFilterEvaluationContexts.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Feature {
    @JsonProperty("id")
    private String id;

    @JsonProperty("description")
    private String description;

    @JsonProperty("enabled")
    private boolean enabled;

    @JsonProperty("conditions")
    @NonNull
    private Conditions conditions = new Conditions();

    @JsonProperty("allocation")
    private Allocation allocation;

    @JsonProperty("variants")
    private List<VariantReference> variants;

    @JsonProperty("telemetry")
    private Telemetry telemetry = new Telemetry();

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     * @return Feature
     */
    public Feature setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * @return the enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @param enabled the enabled to set
     * @return Feature
     */
    public Feature setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     * @return Feature
     */
    public Feature setDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * @return the conditions
     */
    public Conditions getConditions() {
        return conditions;
    }

    /**
     * @param conditions the conditions to set
     * @return Feature
     */
    public Feature setConditions(Conditions conditions) {
        this.conditions = conditions;
        return this;
    }

    /**
     * @return the allocation
     */
    public Allocation getAllocation() {
        return allocation;
    }

    /**
     * @param allocation the allocation to set
     * @return Feature
     */
    public Feature setAllocation(Allocation allocation) {
        this.allocation = allocation;
        return this;
    }

    /**
     * @return the variants
     */
    public List<VariantReference> getVariants() {
        return variants;
    }

    /**
     * @param variants the variants to set
     * @return Feature
     */
    public Feature setVariants(List<VariantReference> variants) {
        this.variants = variants;
        return this;
    }

    /**
     * @return the telemetry
     */
    public Telemetry getTelemetry() {
        return telemetry;
    }

    /**
     * @param telemetry the telemetry to set
     */
    public Feature setTelemetry(Telemetry telemetry) {
        this.telemetry = telemetry;
        return this;
    }

}
