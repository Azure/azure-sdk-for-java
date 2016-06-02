/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;


/**
 * Identifies an object.
 */
public class NameIdentifier {
    /**
     * Name of the object.
     */
    private String name;

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
     * @return the NameIdentifier object itself.
     */
    public NameIdentifier withName(String name) {
        this.name = name;
        return this;
    }

}
