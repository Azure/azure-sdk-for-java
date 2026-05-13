// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration.models;

import java.util.Map;

/**
 * Telemetry configuration for a feature flag.
 */
public final class FeatureFlagTelemetry {
    private final boolean enabled;
    private Map<String, String> metadata;

    /**
     * Creates an instance of FeatureFlagTelemetry.
     *
     * @param enabled whether telemetry is enabled.
     */
    public FeatureFlagTelemetry(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Gets whether telemetry is enabled.
     *
     * @return true if telemetry is enabled.
     */
    public boolean isEnabled() {
        return this.enabled;
    }

    /**
     * Gets the metadata to include on outbound telemetry.
     *
     * @return the metadata.
     */
    public Map<String, String> getMetadata() {
        return this.metadata;
    }

    /**
     * Sets the metadata to include on outbound telemetry.
     *
     * @param metadata the metadata.
     * @return the updated FeatureFlagTelemetry object.
     */
    public FeatureFlagTelemetry setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }
}
