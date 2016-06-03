/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation.api;


/**
 * Policy definition.
 */
public class PolicyDefinitionInner {
    /**
     * Gets or sets the policy definition properties.
     */
    private PolicyDefinitionProperties properties;

    /**
     * Gets or sets the policy definition name.
     */
    private String name;

    /**
     * Get the properties value.
     *
     * @return the properties value
     */
    public PolicyDefinitionProperties properties() {
        return this.properties;
    }

    /**
     * Set the properties value.
     *
     * @param properties the properties value to set
     * @return the PolicyDefinitionInner object itself.
     */
    public PolicyDefinitionInner withProperties(PolicyDefinitionProperties properties) {
        this.properties = properties;
        return this;
    }

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
     * @return the PolicyDefinitionInner object itself.
     */
    public PolicyDefinitionInner withName(String name) {
        this.name = name;
        return this;
    }

}
