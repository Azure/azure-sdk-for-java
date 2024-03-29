// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.hybridkubernetes.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.hybridkubernetes.fluent.models.OperationInner;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** The paginated list of connected cluster API operations. */
@Fluent
public final class OperationList {
    /*
     * The list of connected cluster API operations.
     */
    @JsonProperty(value = "value", access = JsonProperty.Access.WRITE_ONLY)
    private List<OperationInner> value;

    /*
     * The link to fetch the next page of connected cluster API operations.
     */
    @JsonProperty(value = "nextLink")
    private String nextLink;

    /** Creates an instance of OperationList class. */
    public OperationList() {
    }

    /**
     * Get the value property: The list of connected cluster API operations.
     *
     * @return the value value.
     */
    public List<OperationInner> value() {
        return this.value;
    }

    /**
     * Get the nextLink property: The link to fetch the next page of connected cluster API operations.
     *
     * @return the nextLink value.
     */
    public String nextLink() {
        return this.nextLink;
    }

    /**
     * Set the nextLink property: The link to fetch the next page of connected cluster API operations.
     *
     * @param nextLink the nextLink value to set.
     * @return the OperationList object itself.
     */
    public OperationList withNextLink(String nextLink) {
        this.nextLink = nextLink;
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
