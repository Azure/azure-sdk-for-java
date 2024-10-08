// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.desktopvirtualization.models;

import com.azure.resourcemanager.desktopvirtualization.fluent.models.ResourceProviderOperationInner;

/**
 * An immutable client-side representation of ResourceProviderOperation.
 */
public interface ResourceProviderOperation {
    /**
     * Gets the name property: Operation name, in format of {provider}/{resource}/{operation}.
     * 
     * @return the name value.
     */
    String name();

    /**
     * Gets the display property: Display metadata associated with the operation.
     * 
     * @return the display value.
     */
    ResourceProviderOperationDisplay display();

    /**
     * Gets the isDataAction property: Is a data action.
     * 
     * @return the isDataAction value.
     */
    Boolean isDataAction();

    /**
     * Gets the properties property: Properties of the operation.
     * 
     * @return the properties value.
     */
    OperationProperties properties();

    /**
     * Gets the inner com.azure.resourcemanager.desktopvirtualization.fluent.models.ResourceProviderOperationInner
     * object.
     * 
     * @return the inner object.
     */
    ResourceProviderOperationInner innerModel();
}
