// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.feature.management.models;

import java.util.List;

import org.springframework.lang.NonNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This class represents a complete feature flag definition including its identity,
 * description, enabled state, conditions for evaluation, variant allocation,
 * and variant references for feature flags that support multiple variations.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FeatureDefinition {

    /**
     * Creates a new instance of the Feature class.
     */
    public FeatureDefinition() {
    }

    /**
     * The unique identifier for this feature flag.
     * This represents the name of the feature as stored in Azure App Configuration.
     */
    @JsonProperty("id")
    private String id;

    /**
     * A human-readable description of the feature flag and its purpose.
     * This provides context about what the feature flag controls.
     */
    @JsonProperty("description")
    private String description;

    /**
     * The enabled state of the feature flag.
     * When true, the feature is enabled by default, though conditions may still apply.
     * When false, the feature is disabled by default.
     */
    @JsonProperty("enabled")
    private boolean enabled;

    /**
     * The set of conditions that determine when this feature flag should be enabled.
     * These conditions contain feature filters and their evaluation logic.
     */
    @JsonProperty("conditions")
    @NonNull
    private Conditions conditions = new Conditions();

    /**
     * The allocation strategy for this feature flag when using variants.
     * Determines how users or requests are assigned to specific variants.
     */
    @JsonProperty("allocation")
    private Allocation allocation;
    
    /**
     * The list of variant references that define the different variations
     * of this feature flag when it supports multiple implementations.
     */
    @JsonProperty("variants")
    private List<VariantReference> variants;
    
    /**
     * The telemetry configuration for this feature flag.
     * Controls whether events related to this feature are logged
     * and what additional metadata is included.
     */
    @JsonProperty("telemetry")
    private FeatureTelemetry telemetry = new FeatureTelemetry();

    /**
     * Gets the unique identifier of this feature flag.
     * 
     * @return the feature flag's identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique identifier of this feature flag.
     * 
     * @param id the feature flag identifier to set
     * @return the updated Feature instance for method chaining
     */
    public FeatureDefinition setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Determines whether this feature flag is enabled by default.
     * Even when enabled, the flag may still be controlled by conditions and filters.
     * 
     * @return true if the feature flag is enabled, false otherwise
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets whether this feature flag is enabled by default.
     * 
     * @param enabled true to enable the feature flag, false to disable it
     * @return the updated Feature instance for method chaining
     */
    public FeatureDefinition setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     * Gets the human-readable description of this feature flag.
     * 
     * @return the description of the feature flag
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the human-readable description of this feature flag.
     * This provides context about what the feature flag controls.
     * 
     * @param description the description to set for the feature flag
     * @return the updated Feature instance for method chaining
     */
    public FeatureDefinition setDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Gets the set of conditions that determine when this feature flag should be enabled.
     * The conditions contain feature filters and their evaluation logic.
     * 
     * @return the conditions for feature flag evaluation
     */
    public Conditions getConditions() {
        return conditions;
    }

    /**
     * Sets the conditions that determine when this feature flag should be enabled.
     * These conditions define the feature filters and logic for evaluating 
     * whether the feature should be enabled for a specific request.
     * 
     * @param conditions the conditions to set for feature flag evaluation
     * @return the updated Feature instance for method chaining
     */
    public FeatureDefinition setConditions(Conditions conditions) {
        this.conditions = conditions;
        return this;
    }

    /**
     * Gets the allocation strategy for this feature flag when using variants.
     * The allocation defines how users or requests are assigned to specific variants
     * through mechanisms like user targeting, percentile rollout, or group assignment.
     * 
     * @return the allocation strategy for variant assignment
     */
    public Allocation getAllocation() {
        return allocation;
    }

    /**
     * Sets the allocation strategy for this feature flag when using variants.
     * The allocation controls how users or requests are assigned to specific
     * variants through user targeting, percentile rollout, or group assignment.
     * 
     * @param allocation the allocation strategy to set for variant assignment
     * @return the updated Feature instance for method chaining
     */
    public FeatureDefinition setAllocation(Allocation allocation) {
        this.allocation = allocation;
        return this;
    }

    /**
     * Gets the list of variant references that define the different variations
     * of this feature flag. These variants represent different implementations
     * or configurations that can be assigned to users when the feature is enabled.
     * 
     * @return the list of variant references for this feature flag
     */
    public List<VariantReference> getVariants() {
        return variants;
    }

    /**
     * Sets the list of variant references that define the different variations
     * of this feature flag. These variants represent different implementations
     * or configurations that can be dynamically assigned when the feature is enabled.
     * 
     * @param variants the list of variant references to set for this feature flag
     * @return the updated Feature instance for method chaining
     */
    public FeatureDefinition setVariants(List<VariantReference> variants) {
        this.variants = variants;
        return this;
    }

    /**
     * Gets the telemetry configuration for this feature flag.
     * The telemetry configuration controls whether events related to this 
     * feature flag should be logged and what additional metadata should be included.
     * 
     * @return the telemetry configuration for this feature flag
     */
    public FeatureTelemetry getTelemetry() {
        return telemetry;
    }

    /**
     * Sets the telemetry configuration for this feature flag.
     * This controls whether events related to this feature flag 
     * should be logged and what additional metadata should be included.
     * 
     * @param telemetry the telemetry configuration to set for this feature flag
     * @return the updated Feature instance for method chaining
     */
    public FeatureDefinition setTelemetry(FeatureTelemetry telemetry) {
        this.telemetry = telemetry;
        return this;
    }

}
