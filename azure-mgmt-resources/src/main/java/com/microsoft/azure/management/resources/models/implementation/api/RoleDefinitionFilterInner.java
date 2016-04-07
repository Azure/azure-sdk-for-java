/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.models.implementation.api;


/**
 * Role Definitions filter.
 */
public class RoleDefinitionFilterInner {
    /**
     * Returns role definition with the specific name.
     */
    private String roleName;

    /**
     * Get the roleName value.
     *
     * @return the roleName value
     */
    public String roleName() {
        return this.roleName;
    }

    /**
     * Set the roleName value.
     *
     * @param roleName the roleName value to set
     * @return the RoleDefinitionFilterInner object itself.
     */
    public RoleDefinitionFilterInner setRoleName(String roleName) {
        this.roleName = roleName;
        return this;
    }

}
