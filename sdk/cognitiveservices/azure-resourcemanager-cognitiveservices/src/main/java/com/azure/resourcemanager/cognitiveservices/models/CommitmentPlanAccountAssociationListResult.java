// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.cognitiveservices.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.cognitiveservices.fluent.models.CommitmentPlanAccountAssociationInner;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** The list of cognitive services Commitment Plan Account Association operation response. */
@Fluent
public final class CommitmentPlanAccountAssociationListResult {
    /*
     * The link used to get the next page of Commitment Plan Account Association.
     */
    @JsonProperty(value = "nextLink")
    private String nextLink;

    /*
     * Gets the list of Cognitive Services Commitment Plan Account Association and their properties.
     */
    @JsonProperty(value = "value", access = JsonProperty.Access.WRITE_ONLY)
    private List<CommitmentPlanAccountAssociationInner> value;

    /** Creates an instance of CommitmentPlanAccountAssociationListResult class. */
    public CommitmentPlanAccountAssociationListResult() {
    }

    /**
     * Get the nextLink property: The link used to get the next page of Commitment Plan Account Association.
     *
     * @return the nextLink value.
     */
    public String nextLink() {
        return this.nextLink;
    }

    /**
     * Set the nextLink property: The link used to get the next page of Commitment Plan Account Association.
     *
     * @param nextLink the nextLink value to set.
     * @return the CommitmentPlanAccountAssociationListResult object itself.
     */
    public CommitmentPlanAccountAssociationListResult withNextLink(String nextLink) {
        this.nextLink = nextLink;
        return this;
    }

    /**
     * Get the value property: Gets the list of Cognitive Services Commitment Plan Account Association and their
     * properties.
     *
     * @return the value value.
     */
    public List<CommitmentPlanAccountAssociationInner> value() {
        return this.value;
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
