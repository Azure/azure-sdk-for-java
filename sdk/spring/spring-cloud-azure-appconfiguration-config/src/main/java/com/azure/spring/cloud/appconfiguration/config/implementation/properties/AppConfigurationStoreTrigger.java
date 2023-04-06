// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation.properties;

import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.EMPTY_LABEL;

import org.springframework.util.Assert;

import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotNull;

/**
 * Properties on what Triggers are checked before a refresh is triggered.
 */
public final class AppConfigurationStoreTrigger {

    @NotNull
    private String key;

    private String label;

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
     * @return the label
     */
    public String getLabel() {
        return mapLabel(label);
    }

    /**
     * @param label the label to set
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Validates key isn't null
     */
    @PostConstruct
    public void validateAndInit() {
        Assert.notNull(key, "All Triggers need a key value set.");
    }

    private String mapLabel(String label) {
        if (label == null || "".equals(label)) {
            return EMPTY_LABEL;
        }
        return label.trim();
    }
}
