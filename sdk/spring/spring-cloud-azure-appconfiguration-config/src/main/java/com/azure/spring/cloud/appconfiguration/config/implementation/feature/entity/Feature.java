// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation.feature.entity;

import java.util.List;

import com.azure.data.appconfiguration.models.FeatureFlagConfigurationSetting;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Azure App Configuration Feature Flag.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class Feature {

    @JsonProperty("id")
    private String id;

    @JsonProperty("description")
    private String description;

    @JsonProperty("enabled")
    private boolean enabled;

    @JsonProperty("conditions")
    private Conditions conditions;

    @JsonProperty("variants")
    private List<Variant> variants;

    @JsonProperty("allocation")
    private Allocation allocation;

    @JsonProperty("telemetry")
    private FeatureTelemetry telemetry;

    /**
     * Feature Flag object.
     */
    public Feature() {
    }

    /**
     * Feature Flag object.
     *
     * @param key Name of the Feature Flag
     * @param featureItem Configurations of the Feature Flag.
     */
    public Feature(FeatureFlagConfigurationSetting featureFlag, String requirementType, FeatureTelemetry telemetry) {
        this.id = featureFlag.getFeatureId();
        this.description = featureFlag.getDescription();
        this.enabled = featureFlag.isEnabled();

        this.conditions = new Conditions(featureFlag.getClientFilters(), requirementType);

        this.setTelemetry(telemetry);
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @param enabled the enabled to set
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @return the conditions
     */
    public Conditions getConditions() {
        return conditions;
    }

    /**
     * @param conditions the conditions to set
     */
    public void setConditions(Conditions conditions) {
        this.conditions = conditions;
    }

    /**
     * @return the variants
     */
    public List<Variant> getVariants() {
        return variants;
    }

    /**
     * @param variants the variants to set
     */
    public void setVariants(List<Variant> variants) {
        this.variants = variants;
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
     * @return the telemetry
     */
    public FeatureTelemetry getTelemetry() {
        return telemetry;
    }

    /**
     * @param telemetry the telemetry to set
     */
    public void setTelemetry(FeatureTelemetry telemetry) {
        this.telemetry = telemetry;
    }
}
