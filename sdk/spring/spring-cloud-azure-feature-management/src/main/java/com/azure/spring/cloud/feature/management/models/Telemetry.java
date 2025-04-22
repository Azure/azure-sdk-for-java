package com.azure.spring.cloud.feature.management.models;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Telemetry {

    private boolean enabled;

    private Map<String, String> metadata;

    /**
     * @return the enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @param enabled the enabled to set
     */
    public Telemetry setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     * @return the metadata
     */
    public Map<String, String> getMetadata() {
        return metadata;
    }

    /**
     * @param metadata the metadata to set
     */
    public Telemetry setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }

}
