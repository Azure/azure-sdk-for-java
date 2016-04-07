/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.models.implementation.api;


/**
 * Role assignment create parameters.
 */
public class RoleAssignmentCreateParametersInner {
    /**
     * Gets or sets role assignment properties.
     */
    private RoleAssignmentProperties properties;

    /**
     * Get the properties value.
     *
     * @return the properties value
     */
    public RoleAssignmentProperties properties() {
        return this.properties;
    }

    /**
     * Set the properties value.
     *
     * @param properties the properties value to set
     * @return the RoleAssignmentCreateParametersInner object itself.
     */
    public RoleAssignmentCreateParametersInner setProperties(RoleAssignmentProperties properties) {
        this.properties = properties;
        return this;
    }

}
