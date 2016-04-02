/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.models.dto.toplevel;


import com.microsoft.azure.management.resources.models.dto.PolicyDefinitionProperties;

/**
 * Policy definition.
 */
public class PolicyDefinition {
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
    public PolicyDefinitionProperties getProperties() {
        return this.properties;
    }

    /**
     * Set the properties value.
     *
     * @param properties the properties value to set
     */
    public void setProperties(PolicyDefinitionProperties properties) {
        this.properties = properties;
    }

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

}
