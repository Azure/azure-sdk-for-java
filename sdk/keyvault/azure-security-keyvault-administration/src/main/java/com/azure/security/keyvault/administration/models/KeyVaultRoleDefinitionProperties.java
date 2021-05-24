// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration.models;

import com.azure.core.annotation.Immutable;

import java.util.List;

/**
 * A class that defines a role definition's properties.
 */
@Immutable
public final class KeyVaultRoleDefinitionProperties {
    private final String roleName;
    private final String description;
    private final KeyVaultRoleType roleType;
    private final List<KeyVaultPermission> permissions;
    private final List<KeyVaultRoleScope> assignableScopes;

    /**
     * Creates a new {@link KeyVaultRoleDefinitionProperties role definition properties} object with the specified
     * details.
     *
     * @param roleName The name of the role.
     * @param roleDescription The description of the role.
     * @param roleType The type of the role.
     * @param permissions The {@link KeyVaultPermission permissions} the {@link KeyVaultRoleDefinition role definition}
     * has.
     * @param assignableScopes The assignable scopes of the {@link KeyVaultRoleDefinition role definition}.
     */
    public KeyVaultRoleDefinitionProperties(String roleName, String roleDescription, KeyVaultRoleType roleType,
                                            List<KeyVaultPermission> permissions,
                                            List<KeyVaultRoleScope> assignableScopes) {
        this.roleName = roleName;
        this.description = roleDescription;
        this.roleType = roleType;
        this.permissions = permissions;
        this.assignableScopes = assignableScopes;
    }

    /**
     * Get the role name.
     *
     * @return The role name.
     */
    public String getRoleName() {
        return roleName;
    }

    /**
     * Get the role description.
     *
     * @return The role description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get the role type.
     *
     * @return The role type.
     */
    public KeyVaultRoleType getRoleType() {
        return roleType;
    }

    /**
     * Get the {@link KeyVaultRoleDefinition role definition}'s {@link KeyVaultPermission permissions}.
     *
     * @return The {@link KeyVaultRoleDefinition role definition}'s {@link KeyVaultPermission permissions}.
     */
    public List<KeyVaultPermission> getPermissions() {
        return permissions;
    }

    /**
     * Get the {@link KeyVaultRoleDefinition role definition}'s assignable scopes.
     *
     * @return The {@link KeyVaultRoleDefinition role definition}'s assignable scopes.
     */
    public List<KeyVaultRoleScope> getAssignableScopes() {
        return assignableScopes;
    }
}
