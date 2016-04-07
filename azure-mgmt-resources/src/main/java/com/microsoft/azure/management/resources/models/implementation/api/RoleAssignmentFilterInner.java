/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.models.implementation.api;


/**
 * Role Assignments filter.
 */
public class RoleAssignmentFilterInner {
    /**
     * Returns role assignment of the specific principal.
     */
    private String principalId;

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
     * @return the RoleAssignmentFilterInner object itself.
     */
    public RoleAssignmentFilterInner setPrincipalId(String principalId) {
        this.principalId = principalId;
        return this;
    }

}
