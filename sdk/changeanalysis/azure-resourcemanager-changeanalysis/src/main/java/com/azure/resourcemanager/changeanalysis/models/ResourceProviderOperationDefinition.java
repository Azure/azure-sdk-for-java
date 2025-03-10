// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.changeanalysis.models;

import com.azure.resourcemanager.changeanalysis.fluent.models.ResourceProviderOperationDefinitionInner;

/**
 * An immutable client-side representation of ResourceProviderOperationDefinition.
 */
public interface ResourceProviderOperationDefinition {
    /**
     * Gets the name property: The resource provider operation name.
     * 
     * @return the name value.
     */
    String name();

    /**
     * Gets the display property: The resource provider operation details.
     * 
     * @return the display value.
     */
    ResourceProviderOperationDisplay display();

    /**
     * Gets the inner com.azure.resourcemanager.changeanalysis.fluent.models.ResourceProviderOperationDefinitionInner
     * object.
     * 
     * @return the inner object.
     */
    ResourceProviderOperationDefinitionInner innerModel();
}
