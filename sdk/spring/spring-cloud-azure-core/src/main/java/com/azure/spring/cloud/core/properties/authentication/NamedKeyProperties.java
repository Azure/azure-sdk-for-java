// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.properties.authentication;

/**
 * Represents a pojo containing the key and the name of the key.
 */
public final class NamedKeyProperties {

    private String name;
    private String key;

    /**
     * Get the name of the named key.
     * @return The name of the named key.
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name of the named key.
     * @param name The name of the named key.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the key of the named key.
     * @return The key of the named key.
     */
    public String getKey() {
        return key;
    }

    /**
     * Set the key of the named key.
     * @param key The key of the named key.
     */
    public void setKey(String key) {
        this.key = key;
    }
}
