// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration.models;

import com.azure.core.annotation.Immutable;
import com.azure.security.keyvault.administration.implementation.KeyVaultErrorCodeStrings;

import java.util.Objects;

/**
 * A class that defines a role assignment's properties.
 */
@Immutable
public final class KeyVaultRoleAssignmentProperties {
    private final String roleDefinitionId;
    private final String principalId;
    private final KeyVaultRoleScope roleScope;

    /**
     * Creates a new {@link KeyVaultRoleAssignmentProperties role assignment properties} object with the specified
     * details.
     *
     * @param roleDefinitionId The {@link KeyVaultRoleDefinition role definition} ID used in the
     * {@link KeyVaultRoleAssignment role assignment}.
     * @param principalId The principal ID assigned to the role. This maps to the ID inside the Active Directory.
     * It can point to a user, service principal, or security group.*
     * @param roleScope The {@link KeyVaultRoleScope scope} of this {@link KeyVaultRoleAssignment role assignment}.
     */
    public KeyVaultRoleAssignmentProperties(String roleDefinitionId, String principalId, KeyVaultRoleScope roleScope) {
        this.roleDefinitionId = roleDefinitionId;
        this.principalId = principalId;
        this.roleScope = roleScope;
    }

    /**
     * Get the {@link KeyVaultRoleDefinition role definition} ID used in the {@link KeyVaultRoleAssignment role
     * assignment}.
     *
     * @return The {@link KeyVaultRoleDefinition role definition} ID.
     */
    public String getRoleDefinitionId() {
        return roleDefinitionId;
    }

    /**
     * Get the principal ID assigned to the role.
     *
     * @return The principal ID.
     */
    public String getPrincipalId() {
        return principalId;
    }

    /**
     * Get the {@link KeyVaultRoleAssignment role assignment} {@link KeyVaultRoleScope scope}.
     *
     * @return The {@link KeyVaultRoleAssignment role assignment} {@link KeyVaultRoleScope scope}.
     */
    public KeyVaultRoleScope getRoleScope() {
        return roleScope;
    }
}
