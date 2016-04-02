/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.models.dto;


/**
 * Role assignment create parameters.
 */
public class RoleAssignmentCreateParameters {
    /**
     * Gets or sets role assignment properties.
     */
    private RoleAssignmentProperties properties;

    /**
     * Get the properties value.
     *
     * @return the properties value
     */
    public RoleAssignmentProperties getProperties() {
        return this.properties;
    }

    /**
     * Set the properties value.
     *
     * @param properties the properties value to set
     */
    public void setProperties(RoleAssignmentProperties properties) {
        this.properties = properties;
    }

}
