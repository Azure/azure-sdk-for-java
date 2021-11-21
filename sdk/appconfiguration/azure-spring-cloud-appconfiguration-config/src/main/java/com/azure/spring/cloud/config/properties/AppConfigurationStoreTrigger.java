// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.properties;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;

import org.springframework.util.Assert;
import static com.azure.spring.cloud.config.AppConfigurationConstants.EMPTY_LABEL;

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

    @PostConstruct
    public void validateAndInit() {
        Assert.notNull(key, "All Triggers need a key value set.");
    }

    @Override
    public String toString() {
        if (label == null) {
            return key + "/";
        }
        return key + "/" + label;
    }

    private String mapLabel(String label) {
        if (label == null || label.equals("")) {
            return EMPTY_LABEL;
        }
        return label.trim();
    }
}
