/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.implementation.api;


/**
 * Represents a name-value pair.
 */
public class NameValuePair {
    /**
     * Gets or sets the name in the name-value pair.
     */
    private String name;

    /**
     * Gets or sets the value in the name-value pair.
     */
    private String value;

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
     * @return the NameValuePair object itself.
     */
    public NameValuePair setName(String name) {
        this.name = name;
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
     * @return the NameValuePair object itself.
     */
    public NameValuePair setValue(String value) {
        this.value = value;
        return this;
    }

}
