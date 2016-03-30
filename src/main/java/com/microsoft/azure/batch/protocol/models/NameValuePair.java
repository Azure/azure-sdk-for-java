/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.models;


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
    public String getName() {
        return this.name;
    }

    /**
     * Set the name value.
     *
     * @param name the name value to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the value value.
     *
     * @return the value value
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Set the value value.
     *
     * @param value the value value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

}
