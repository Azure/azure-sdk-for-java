// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.vmwarecloudsimple.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.vmwarecloudsimple.fluent.models.PrivateCloudInner;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** List of private clouds. */
@Fluent
public final class PrivateCloudList {
    /*
     * Link for next list of Private Clouds
     */
    @JsonProperty(value = "nextLink")
    private String nextLink;

    /*
     * the list of private clouds
     */
    @JsonProperty(value = "value")
    private List<PrivateCloudInner> value;

    /** Creates an instance of PrivateCloudList class. */
    public PrivateCloudList() {
    }

    /**
     * Get the nextLink property: Link for next list of Private Clouds.
     *
     * @return the nextLink value.
     */
    public String nextLink() {
        return this.nextLink;
    }

    /**
     * Set the nextLink property: Link for next list of Private Clouds.
     *
     * @param nextLink the nextLink value to set.
     * @return the PrivateCloudList object itself.
     */
    public PrivateCloudList withNextLink(String nextLink) {
        this.nextLink = nextLink;
        return this;
    }

    /**
     * Get the value property: the list of private clouds.
     *
     * @return the value value.
     */
    public List<PrivateCloudInner> value() {
        return this.value;
    }

    /**
     * Set the value property: the list of private clouds.
     *
     * @param value the value value to set.
     * @return the PrivateCloudList object itself.
     */
    public PrivateCloudList withValue(List<PrivateCloudInner> value) {
        this.value = value;
        return this;
    }

    /**
     * Validates the instance.
     *
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (value() != null) {
            value().forEach(e -> e.validate());
        }
    }
}
