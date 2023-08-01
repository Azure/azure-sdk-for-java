// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.properties.client;

import java.util.List;

/**
 * Describing a header such as a http header.
 */
public final class HeaderProperties {

    /**
     * The name of the header.
     */
    private String name;

    /**
     * List of values of the header.
     */
    private List<String> values;

    /**
     * Get the header name.
     *
     * @return the name of this header.
     */
    public String getName() {
        return name;
    }

    /**
     * Set the header name.
     * @param name The name of this header.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the values of this header.
     * @return The values of this header.
     */
    public List<String> getValues() {
        return values;
    }

    /**
     * Set the values of this header.
     * @param values The values of this header.
     */
    public void setValues(List<String> values) {
        this.values = values;
    }
}
