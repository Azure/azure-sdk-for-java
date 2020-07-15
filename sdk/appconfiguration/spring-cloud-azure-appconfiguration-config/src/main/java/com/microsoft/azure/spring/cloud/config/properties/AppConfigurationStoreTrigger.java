/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config.properties;

import javax.annotation.PostConstruct;

import org.springframework.util.Assert;

public class AppConfigurationStoreTrigger {

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
        return label;
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
}
