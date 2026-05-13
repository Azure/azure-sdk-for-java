// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration.models;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * Represents a feature flag in Azure App Configuration. Feature flags can be used to enable or disable features
 * dynamically, with support for conditional evaluation via filters, variant definitions, and allocation rules.
 *
 * <p>Use the dedicated feature flag methods on {@link com.azure.data.appconfiguration.ConfigurationClient} to
 * create, retrieve, update, list, and delete feature flags.</p>
 */
public final class FeatureFlag {
    private String name;
    private Boolean enabled;
    private String label;
    private String description;
    private FeatureFlagConditions conditions;
    private List<FeatureFlagVariant> variants;
    private FeatureFlagAllocation allocation;
    private FeatureFlagTelemetry telemetry;
    private Map<String, String> tags;
    private OffsetDateTime lastModified;
    private String etag;

    /**
     * Creates an instance of FeatureFlag.
     */
    public FeatureFlag() {
    }

    /**
     * Gets the name of the feature flag.
     *
     * @return the name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the name of the feature flag. This is typically set by the service and should not need to be set manually.
     *
     * @param name the name.
     * @return the updated FeatureFlag object.
     */
    FeatureFlag setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Gets the enabled state of the feature flag.
     *
     * @return true if the feature flag is enabled, false otherwise.
     */
    public Boolean isEnabled() {
        return this.enabled;
    }

    /**
     * Sets the enabled state of the feature flag.
     *
     * @param enabled the enabled state.
     * @return the updated FeatureFlag object.
     */
    public FeatureFlag setEnabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     * Gets the label the feature flag belongs to.
     *
     * @return the label.
     */
    public String getLabel() {
        return this.label;
    }

    /**
     * Sets the label. This is typically set by the service and should not need to be set manually.
     *
     * @param label the label.
     * @return the updated FeatureFlag object.
     */
    FeatureFlag setLabel(String label) {
        this.label = label;
        return this;
    }

    /**
     * Gets the description of the feature flag.
     *
     * @return the description.
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Sets the description of the feature flag.
     *
     * @param description the description.
     * @return the updated FeatureFlag object.
     */
    public FeatureFlag setDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Gets the conditions that must be met for the feature flag to be enabled.
     *
     * @return the conditions.
     */
    public FeatureFlagConditions getConditions() {
        return this.conditions;
    }

    /**
     * Sets the conditions that must be met for the feature flag to be enabled.
     *
     * @param conditions the conditions.
     * @return the updated FeatureFlag object.
     */
    public FeatureFlag setConditions(FeatureFlagConditions conditions) {
        this.conditions = conditions;
        return this;
    }

    /**
     * Gets the variants of the feature flag.
     *
     * @return the variants.
     */
    public List<FeatureFlagVariant> getVariants() {
        return this.variants;
    }

    /**
     * Sets the variants of the feature flag.
     *
     * @param variants the variants.
     * @return the updated FeatureFlag object.
     */
    public FeatureFlag setVariants(List<FeatureFlagVariant> variants) {
        this.variants = variants;
        return this;
    }

    /**
     * Gets the allocation of the feature flag.
     *
     * @return the allocation.
     */
    public FeatureFlagAllocation getAllocation() {
        return this.allocation;
    }

    /**
     * Sets the allocation of the feature flag.
     *
     * @param allocation the allocation.
     * @return the updated FeatureFlag object.
     */
    public FeatureFlag setAllocation(FeatureFlagAllocation allocation) {
        this.allocation = allocation;
        return this;
    }

    /**
     * Gets the telemetry settings of the feature flag.
     *
     * @return the telemetry settings.
     */
    public FeatureFlagTelemetry getTelemetry() {
        return this.telemetry;
    }

    /**
     * Sets the telemetry settings of the feature flag.
     *
     * @param telemetry the telemetry settings.
     * @return the updated FeatureFlag object.
     */
    public FeatureFlag setTelemetry(FeatureFlagTelemetry telemetry) {
        this.telemetry = telemetry;
        return this;
    }

    /**
     * Gets the tags of the feature flag.
     *
     * @return the tags.
     */
    public Map<String, String> getTags() {
        return this.tags;
    }

    /**
     * Sets the tags of the feature flag.
     *
     * @param tags the tags.
     * @return the updated FeatureFlag object.
     */
    public FeatureFlag setTags(Map<String, String> tags) {
        this.tags = tags;
        return this;
    }

    /**
     * Gets the date the feature flag was last modified.
     *
     * @return the last modified date.
     */
    public OffsetDateTime getLastModified() {
        return this.lastModified;
    }

    /**
     * Sets the last modified date. This is typically set by the service.
     *
     * @param lastModified the last modified date.
     * @return the updated FeatureFlag object.
     */
    FeatureFlag setLastModified(OffsetDateTime lastModified) {
        this.lastModified = lastModified;
        return this;
    }

    /**
     * Gets the etag of the feature flag.
     *
     * @return the etag.
     */
    public String getEtag() {
        return this.etag;
    }

    /**
     * Sets the etag. This is typically set by the service.
     *
     * @param etag the etag.
     * @return the updated FeatureFlag object.
     */
    FeatureFlag setEtag(String etag) {
        this.etag = etag;
        return this;
    }
}
