// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.timeseriesinsights.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.logging.ClientLogger;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/** Properties required to create any resource tracked by Azure Resource Manager. */
@Fluent
public class CreateOrUpdateTrackedResourceProperties {
    @JsonIgnore private final ClientLogger logger = new ClientLogger(CreateOrUpdateTrackedResourceProperties.class);

    /*
     * The location of the resource.
     */
    @JsonProperty(value = "location", required = true)
    private String location;

    /*
     * Key-value pairs of additional properties for the resource.
     */
    @JsonProperty(value = "tags")
    private Map<String, String> tags;

    /**
     * Get the location property: The location of the resource.
     *
     * @return the location value.
     */
    public String location() {
        return this.location;
    }

    /**
     * Set the location property: The location of the resource.
     *
     * @param location the location value to set.
     * @return the CreateOrUpdateTrackedResourceProperties object itself.
     */
    public CreateOrUpdateTrackedResourceProperties withLocation(String location) {
        this.location = location;
        return this;
    }

    /**
     * Get the tags property: Key-value pairs of additional properties for the resource.
     *
     * @return the tags value.
     */
    public Map<String, String> tags() {
        return this.tags;
    }

    /**
     * Set the tags property: Key-value pairs of additional properties for the resource.
     *
     * @param tags the tags value to set.
     * @return the CreateOrUpdateTrackedResourceProperties object itself.
     */
    public CreateOrUpdateTrackedResourceProperties withTags(Map<String, String> tags) {
        this.tags = tags;
        return this;
    }

    /**
     * Validates the instance.
     *
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (location() == null) {
            throw logger
                .logExceptionAsError(
                    new IllegalArgumentException(
                        "Missing required property location in model CreateOrUpdateTrackedResourceProperties"));
        }
    }
}
