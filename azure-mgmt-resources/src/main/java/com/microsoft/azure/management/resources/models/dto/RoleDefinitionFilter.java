/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.models.dto;


/**
 * Role Definitions filter.
 */
public class RoleDefinitionFilter {
    /**
     * Returns role definition with the specific name.
     */
    private String roleName;

    /**
     * Get the roleName value.
     *
     * @return the roleName value
     */
    public String getRoleName() {
        return this.roleName;
    }

    /**
     * Set the roleName value.
     *
     * @param roleName the roleName value to set
     */
    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

}
