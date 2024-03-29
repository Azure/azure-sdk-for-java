// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.support.fluent.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.support.models.ServiceClassificationAnswer;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Output of the service classification API.
 */
@Fluent
public final class ServiceClassificationOutputInner {
    /*
     * Set of problem classification objects classified.
     */
    @JsonProperty(value = "serviceClassificationResults")
    private List<ServiceClassificationAnswer> serviceClassificationResults;

    /**
     * Creates an instance of ServiceClassificationOutputInner class.
     */
    public ServiceClassificationOutputInner() {
    }

    /**
     * Get the serviceClassificationResults property: Set of problem classification objects classified.
     * 
     * @return the serviceClassificationResults value.
     */
    public List<ServiceClassificationAnswer> serviceClassificationResults() {
        return this.serviceClassificationResults;
    }

    /**
     * Set the serviceClassificationResults property: Set of problem classification objects classified.
     * 
     * @param serviceClassificationResults the serviceClassificationResults value to set.
     * @return the ServiceClassificationOutputInner object itself.
     */
    public ServiceClassificationOutputInner
        withServiceClassificationResults(List<ServiceClassificationAnswer> serviceClassificationResults) {
        this.serviceClassificationResults = serviceClassificationResults;
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (serviceClassificationResults() != null) {
            serviceClassificationResults().forEach(e -> e.validate());
        }
    }
}
