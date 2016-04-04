/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.models.dto;


/**
 * Role Assignments filter.
 */
public class RoleAssignmentFilter {
    /**
     * Returns role assignment of the specific principal.
     */
    private String principalId;

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
