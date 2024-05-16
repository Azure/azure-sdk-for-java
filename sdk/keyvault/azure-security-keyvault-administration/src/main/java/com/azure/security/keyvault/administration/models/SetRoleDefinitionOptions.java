// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration.models;

import com.azure.core.annotation.Fluent;

import java.util.List;
import java.util.UUID;

/**
 * Represents the configurable options to create or update a {@link KeyVaultRoleDefinition role definition}.
 */
@Fluent
public final class SetRoleDefinitionOptions {
    private final KeyVaultRoleScope roleScope;
    private final String roleDefinitionName;
    private String roleName;
    private String description;
    private List<KeyVaultPermission> permissions;
    private List<KeyVaultRoleScope> assignableScopes;

    /**
     * Creates an instance of {@link SetRoleDefinitionOptions} with an automatically generated name.
     *
     * @param roleScope The {@link KeyVaultRoleScope role scope} of the {@link KeyVaultRoleDefinition} to create.
     * Managed HSM only supports '/'.
     */
    public SetRoleDefinitionOptions(KeyVaultRoleScope roleScope) {
        this(roleScope, UUID.randomUUID().toString());
    }

    /**
     * Creates an instance of {@link SetRoleDefinitionOptions}.
     *
     * @param roleScope The {@link KeyVaultRoleScope role scope} of the {@link KeyVaultRoleDefinition} to create.
     * Managed HSM only supports '/'.
     * @param roleDefinitionName The name of the {@link KeyVaultRoleDefinition}. It can be any valid UUID.
     */
    public SetRoleDefinitionOptions(KeyVaultRoleScope roleScope, String roleDefinitionName) {
        this.roleScope = roleScope;
        this.roleDefinitionName = roleDefinitionName;
    }

    /**
     * Get the {@link KeyVaultRoleScope role scope} of the {@link KeyVaultRoleDefinition} to create or update.
     *
     * @return The {@link KeyVaultRoleScope role scope} of the {@link KeyVaultRoleDefinition}.
     */
    public KeyVaultRoleScope getRoleScope() {
        return roleScope;
    }

    /**
     * Get the name of the {@link KeyVaultRoleDefinition} to create or update.
     *
     * @return The name of the {@link KeyVaultRoleDefinition}.
     */
    public String getRoleDefinitionName() {
        return roleDefinitionName;
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
     * Set the role name.
     *
     * @param roleName The role name to set.
     *
     * @return The updated {@link SetRoleDefinitionOptions} object.
     */
    public SetRoleDefinitionOptions setRoleName(String roleName) {
        this.roleName = roleName;

        return this;
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
     * Set the role description.
     *
     * @param description The role description to set.
     *
     * @return The updated {@link SetRoleDefinitionOptions} object.
     */
    public SetRoleDefinitionOptions setDescription(String description) {
        this.description = description;

        return this;
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
     * Set the {@link KeyVaultRoleDefinition role definition}'s {@link KeyVaultPermission permissions}.
     *
     * @param permissions The {@link KeyVaultRoleDefinition role definition}'s {@link KeyVaultPermission permissions}
     * to set.
     *
     * @return The updated {@link SetRoleDefinitionOptions} object.
     */
    public SetRoleDefinitionOptions setPermissions(List<KeyVaultPermission> permissions) {
        this.permissions = permissions;

        return this;
    }

    /**
     * Get the {@link KeyVaultRoleDefinition role definition}'s assignable scopes.
     *
     * @return The {@link KeyVaultRoleDefinition role definition}'s assignable scopes.
     */
    public List<KeyVaultRoleScope> getAssignableScopes() {
        return assignableScopes;
    }

    /**
     * Set the {@link KeyVaultRoleDefinition role definition}'s assignable scopes.
     *
     * @param assignableScopes The {@link KeyVaultRoleDefinition role definition}'s assignable scopes to set.
     *
     * @return The updated {@link SetRoleDefinitionOptions} object.
     */
    public SetRoleDefinitionOptions setAssignableScopes(List<KeyVaultRoleScope> assignableScopes) {
        this.assignableScopes = assignableScopes;

        return this;
    }
}
