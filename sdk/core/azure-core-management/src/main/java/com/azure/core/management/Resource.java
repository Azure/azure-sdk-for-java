// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * The Resource model.
 */
public class Resource extends ProxyResource {

    @JsonProperty(required = true)
    private String location;

    private Map<String, String> tags;

    /**
     * Get the location value.
     *
     * @return the geo-location where the resource live.
     */
    public String location() {
        return this.location;
    }

    /**
     * Set the location value.
     *
     * @param location the geo-location where the resource live.
     * @return the resource itself.
     */
    public Resource withLocation(String location) {
        this.location = location;
        return this;
    }

    /**
     * Get the tags value.
     *
     * @return the tags of the resource.
     */
    public Map<String, String> tags() {
        return this.tags;
    }

    /**
     * Set the tags value.
     *
     * @param tags the tags of the resource.
     * @return the resource itself.
     */
    public Resource withTags(Map<String, String> tags) {
        this.tags = tags;
        return this;
    }
}
