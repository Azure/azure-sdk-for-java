// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.avs.models;

import com.azure.core.annotation.Immutable;
import com.azure.resourcemanager.avs.fluent.models.GlobalReachConnectionInner;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** A paged list of global reach connections. */
@Immutable
public final class GlobalReachConnectionList {
    /*
     * The items on a page
     */
    @JsonProperty(value = "value", access = JsonProperty.Access.WRITE_ONLY)
    private List<GlobalReachConnectionInner> value;

    /*
     * URL to get the next page if any
     */
    @JsonProperty(value = "nextLink", access = JsonProperty.Access.WRITE_ONLY)
    private String nextLink;

    /** Creates an instance of GlobalReachConnectionList class. */
    public GlobalReachConnectionList() {
    }

    /**
     * Get the value property: The items on a page.
     *
     * @return the value value.
     */
    public List<GlobalReachConnectionInner> value() {
        return this.value;
    }

    /**
     * Get the nextLink property: URL to get the next page if any.
     *
     * @return the nextLink value.
     */
    public String nextLink() {
        return this.nextLink;
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
