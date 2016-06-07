/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation.api;


/**
 * Resource provider operation information.
 */
public class ResourceProviderOperationDefinitionInner {
    /**
     * Gets or sets the provider operation name.
     */
    private String name;

    /**
     * Gets or sets the display property of the provider operation.
     */
    private ResourceProviderOperationDisplayProperties display;

    /**
     * Get the name value.
     *
     * @return the name value
     */
    public String name() {
        return this.name;
    }

    /**
     * Set the name value.
     *
     * @param name the name value to set
     * @return the ResourceProviderOperationDefinitionInner object itself.
     */
    public ResourceProviderOperationDefinitionInner withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get the display value.
     *
     * @return the display value
     */
    public ResourceProviderOperationDisplayProperties display() {
        return this.display;
    }

    /**
     * Set the display value.
     *
     * @param display the display value to set
     * @return the ResourceProviderOperationDefinitionInner object itself.
     */
    public ResourceProviderOperationDefinitionInner withDisplay(ResourceProviderOperationDisplayProperties display) {
        this.display = display;
        return this;
    }

}
