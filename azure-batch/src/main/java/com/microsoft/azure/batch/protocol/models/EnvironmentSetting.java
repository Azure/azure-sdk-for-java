/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.models;


/**
 * An environment variable to be set on a task process.
 */
public class EnvironmentSetting {
    /**
     * The name of the environment variable.
     */
    private String name;

    /**
     * The value of the environment variable.
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
    public EnvironmentSetting withName(String name) {
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
    public EnvironmentSetting withValue(String value) {
        this.value = value;
        return this;
    }

}
