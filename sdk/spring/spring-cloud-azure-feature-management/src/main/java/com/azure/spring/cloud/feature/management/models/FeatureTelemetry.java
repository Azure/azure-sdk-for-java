// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management.models;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents telemetry configuration for feature management.
 * This class controls whether telemetry is enabled and provides additional
 * metadata for customizing telemetry information.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FeatureTelemetry {

    private boolean enabled;

    private Map<String, String> metadata;

    /**
     * Creates a new instance of the Telemetry class.
     * By default, telemetry is enabled and no metadata is set.
     */
    public FeatureTelemetry() {
        this.enabled = false; // Default to disabled
        this.metadata = new HashMap<>();
    }

    /**
     * Gets whether telemetry is enabled.
     * 
     * @return {@code true} if telemetry is enabled, {@code false} otherwise
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets whether telemetry is enabled.
     * 
     * @param enabled {@code true} to enable telemetry, {@code false} to disable it
     * @return The updated Telemetry instance for method chaining
     */
    public FeatureTelemetry setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     * Gets the metadata associated with telemetry.
     * The metadata contains key-value pairs that provide additional context
     * for telemetry events.
     * 
     * @return A map of metadata key-value pairs
     */
    public Map<String, String> getMetadata() {
        return metadata;
    }

    /**
     * Sets the metadata associated with telemetry.
     * 
     * @param metadata A map of key-value pairs to provide additional context for telemetry events
     * @return The updated Telemetry instance for method chaining
     */
    public FeatureTelemetry setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }

}
