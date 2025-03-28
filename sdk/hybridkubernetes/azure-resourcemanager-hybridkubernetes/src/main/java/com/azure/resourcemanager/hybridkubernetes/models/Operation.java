// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.hybridkubernetes.models;

import com.azure.resourcemanager.hybridkubernetes.fluent.models.OperationInner;

/**
 * An immutable client-side representation of Operation.
 */
public interface Operation {
    /**
     * Gets the name property: Operation name: {Microsoft.Kubernetes}/{resource}/{operation}.
     * 
     * @return the name value.
     */
    String name();

    /**
     * Gets the display property: The object that represents the operation.
     * 
     * @return the display value.
     */
    OperationDisplay display();

    /**
     * Gets the inner com.azure.resourcemanager.hybridkubernetes.fluent.models.OperationInner object.
     * 
     * @return the inner object.
     */
    OperationInner innerModel();
}
