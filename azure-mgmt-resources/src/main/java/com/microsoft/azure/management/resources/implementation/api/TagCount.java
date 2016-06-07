/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation.api;


/**
 * Tag count.
 */
public class TagCount {
    /**
     * Type of count.
     */
    private String type;

    /**
     * Value of count.
     */
    private String value;

    /**
     * Get the type value.
     *
     * @return the type value
     */
    public String type() {
        return this.type;
    }

    /**
     * Set the type value.
     *
     * @param type the type value to set
     * @return the TagCount object itself.
     */
    public TagCount withType(String type) {
        this.type = type;
        return this;
    }

    /**
     * Get the value value.
     *
     * @return the value value
     */
    public String value() {
        return this.value;
    }

    /**
     * Set the value value.
     *
     * @param value the value value to set
     * @return the TagCount object itself.
     */
    public TagCount withValue(String value) {
        this.value = value;
        return this;
    }

}
