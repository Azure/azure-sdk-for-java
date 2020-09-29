// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration.models;

import com.azure.security.keyvault.administration.implementation.KeyVaultErrorCodeStrings;

import java.util.Objects;

/**
 * A class that defines a role assignment's properties.
 */
public final class KeyVaultRoleAssignmentProperties {
    private final String roleDefinitionId;
    private final String principalId;

    /**
     * Creates a new {@link KeyVaultRoleAssignmentProperties role assignment properties} object with the specified
     * details.
     *
     * @param roleDefinitionId The {@link KeyVaultRoleDefinition role definition} ID used in the
     * {@link KeyVaultRoleAssignment role assignment}.
     * @param principalId The principal ID assigned to the role. This maps to the ID inside the Active Directory.
     * It can point to a user, service principal, or security group.
     */
    public KeyVaultRoleAssignmentProperties(String roleDefinitionId, String principalId) {
        Objects.requireNonNull(roleDefinitionId,
            String.format(KeyVaultErrorCodeStrings.getErrorString(KeyVaultErrorCodeStrings.PARAMETER_REQUIRED),
                "'roleDefinitionId' in 'properties'"));
        Objects.requireNonNull(principalId,
            String.format(KeyVaultErrorCodeStrings.getErrorString(KeyVaultErrorCodeStrings.PARAMETER_REQUIRED),
                "'principalId' in 'properties'"));

        this.roleDefinitionId = roleDefinitionId;
        this.principalId = principalId;
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
}
