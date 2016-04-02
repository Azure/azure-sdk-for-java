/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.models.dto;


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
    public String getScope() {
        return this.scope;
    }

    /**
     * Set the scope value.
     *
     * @param scope the scope value to set
     */
    public void setScope(String scope) {
        this.scope = scope;
    }

    /**
     * Get the roleDefinitionId value.
     *
     * @return the roleDefinitionId value
     */
    public String getRoleDefinitionId() {
        return this.roleDefinitionId;
    }

    /**
     * Set the roleDefinitionId value.
     *
     * @param roleDefinitionId the roleDefinitionId value to set
     */
    public void setRoleDefinitionId(String roleDefinitionId) {
        this.roleDefinitionId = roleDefinitionId;
    }

    /**
     * Get the principalId value.
     *
     * @return the principalId value
     */
    public String getPrincipalId() {
        return this.principalId;
    }

    /**
     * Set the principalId value.
     *
     * @param principalId the principalId value to set
     */
    public void setPrincipalId(String principalId) {
        this.principalId = principalId;
    }

}
