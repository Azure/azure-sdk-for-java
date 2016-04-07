/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.models.implementation.api;


/**
 * Role assignment properties with scope.
 */
public class RoleAssignmentPropertiesWithScope {
    /**
     * Gets or sets role assignment scope.
     */
    private String scope;

    /**
     * Gets or sets role definition id.
     */
    private String roleDefinitionId;

    /**
     * Gets or sets principal Id.
     */
    private String principalId;

    /**
     * Get the scope value.
     *
     * @return the scope value
     */
    public String scope() {
        return this.scope;
    }

    /**
     * Set the scope value.
     *
     * @param scope the scope value to set
     * @return the RoleAssignmentPropertiesWithScope object itself.
     */
    public RoleAssignmentPropertiesWithScope setScope(String scope) {
        this.scope = scope;
        return this;
    }

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
     * @return the RoleAssignmentPropertiesWithScope object itself.
     */
    public RoleAssignmentPropertiesWithScope setRoleDefinitionId(String roleDefinitionId) {
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
     * @return the RoleAssignmentPropertiesWithScope object itself.
     */
    public RoleAssignmentPropertiesWithScope setPrincipalId(String principalId) {
        this.principalId = principalId;
        return this;
    }

}
