/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.implementation.api;


/**
 * An environment variable to be set on a task process.
 */
public class EnvironmentSetting {
    /**
     * Gets or sets the name of the environment variable.
     */
    private String name;

    /**
     * Gets or sets the value of the environment variable.
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
     * @return the EnvironmentSetting object itself.
     */
    public EnvironmentSetting setName(String name) {
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
     * @return the EnvironmentSetting object itself.
     */
    public EnvironmentSetting setValue(String value) {
        this.value = value;
        return this;
    }

}
