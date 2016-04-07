/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.models.implementation.api;


/**
 * Role assignment properties.
 */
public class RoleAssignmentProperties {
    /**
     * Gets or sets role definition id.
     */
    private String roleDefinitionId;

    /**
     * Gets or sets principal Id.
     */
    private String principalId;

    /**
     * Get the roleDefinitionId value.
     *
     * @return the roleDefinitionId value
     */
    public String roleDefinitionId() {
        return this.roleDefinitionId;
    }

    /**
     * Set the roleDefinitionId value.
     *
     * @param roleDefinitionId the roleDefinitionId value to set
     * @return the RoleAssignmentProperties object itself.
     */
    public RoleAssignmentProperties setRoleDefinitionId(String roleDefinitionId) {
        this.roleDefinitionId = roleDefinitionId;
        return this;
    }

    /**
     * Get the principalId value.
     *
     * @return the principalId value
     */
    public String principalId() {
        return this.principalId;
    }

    /**
     * Set the principalId value.
     *
     * @param principalId the principalId value to set
     * @return the RoleAssignmentProperties object itself.
     */
    public RoleAssignmentProperties setPrincipalId(String principalId) {
        this.principalId = principalId;
        return this;
    }

}
