// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation.properties;

import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.EMPTY_LABEL;

import org.springframework.util.Assert;

import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotNull;

/**
 * A sentinel configuration setting that is monitored for changes to trigger a
 * configuration refresh.
 */
public final class AppConfigurationStoreTrigger {

    /**
     * Key of the sentinel configuration setting to monitor. Required.
     */
    @NotNull
    private String key;

    /**
     * Label of the sentinel configuration setting to monitor. Defaults to the
     * empty (no label) value when not set.
     */
    private String label;

    /**
     * Returns the key of the sentinel setting.
     *
     * @return the sentinel key
     */
    public String getKey() {
        return key;
    }

    /**
     * Sets the key of the sentinel setting to monitor.
     *
     * @param key the sentinel key
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Returns the label of the sentinel setting, defaulting to the empty-label
     * value when not explicitly set.
     *
     * @return the resolved label
     */
    public String getLabel() {
        return mapLabel(label);
    }

    /**
     * Sets the label of the sentinel setting to monitor.
     *
     * @param label the sentinel label
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Validates that the sentinel key is not null.
     */
    @PostConstruct
    void validateAndInit() {
        Assert.notNull(key, "All Triggers need a key value set.");
    }

    private String mapLabel(String label) {
        if (label == null || "".equals(label)) {
            return EMPTY_LABEL;
        }
        return label.trim();
    }
}
