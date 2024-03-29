// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.streamanalytics.fluent.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.streamanalytics.models.OperationDisplay;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A Stream Analytics REST API operation.
 */
@Fluent
public final class OperationInner {
    /*
     * The name of the operation being performed on this particular object.
     */
    @JsonProperty(value = "name", access = JsonProperty.Access.WRITE_ONLY)
    private String name;

    /*
     * Indicates whether the operation is a data action
     */
    @JsonProperty(value = "isDataAction")
    private Boolean isDataAction;

    /*
     * Contains the localized display information for this particular operation / action.
     */
    @JsonProperty(value = "display", access = JsonProperty.Access.WRITE_ONLY)
    private OperationDisplay display;

    /**
     * Creates an instance of OperationInner class.
     */
    public OperationInner() {
    }

    /**
     * Get the name property: The name of the operation being performed on this particular object.
     * 
     * @return the name value.
     */
    public String name() {
        return this.name;
    }

    /**
     * Get the isDataAction property: Indicates whether the operation is a data action.
     * 
     * @return the isDataAction value.
     */
    public Boolean isDataAction() {
        return this.isDataAction;
    }

    /**
     * Set the isDataAction property: Indicates whether the operation is a data action.
     * 
     * @param isDataAction the isDataAction value to set.
     * @return the OperationInner object itself.
     */
    public OperationInner withIsDataAction(Boolean isDataAction) {
        this.isDataAction = isDataAction;
        return this;
    }

    /**
     * Get the display property: Contains the localized display information for this particular operation / action.
     * 
     * @return the display value.
     */
    public OperationDisplay display() {
        return this.display;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (display() != null) {
            display().validate();
        }
    }
}
