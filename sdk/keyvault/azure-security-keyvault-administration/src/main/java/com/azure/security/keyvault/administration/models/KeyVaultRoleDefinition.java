// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration.models;

import com.azure.core.annotation.Immutable;

import java.util.List;

/**
 * A class that defines a role.
 */
@Immutable
public final class KeyVaultRoleDefinition {
    private final String id;
    private final String name;
    private final KeyVaultRoleDefinitionType type;
    private final String roleName;
    private final String description;
    private final KeyVaultRoleType roleType;
    private final List<KeyVaultPermission> permissions;
    private final List<KeyVaultRoleScope> assignableScopes;

    /**
     * Creates a new {@link KeyVaultRoleDefinition role definition} with the specified details.
     *
     * @param id The ID for this {@link KeyVaultRoleDefinition role definition}.
     * @param name The name for this {@link KeyVaultRoleDefinition role definition}.
     * @param type The type of this {@link KeyVaultRoleDefinition role definition}.
     * @param roleName The name of the role.
     * @param description The description of this {@link KeyVaultRoleDefinition role definition}.
     * @param roleType The type of the role.
     * @param permissions The {@link KeyVaultPermission permissions} the {@link KeyVaultRoleDefinition role definition}
     * has.
     * @param assignableScopes The assignable scopes of the {@link KeyVaultRoleDefinition role definition}.
     */
    public KeyVaultRoleDefinition(String id, String name, KeyVaultRoleDefinitionType type, String roleName,
                                  String description, KeyVaultRoleType roleType, List<KeyVaultPermission> permissions,
                                  List<KeyVaultRoleScope> assignableScopes) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.roleName = roleName;
        this.description = description;
        this.roleType = roleType;
        this.permissions = permissions;
        this.assignableScopes = assignableScopes;
    }

    /**
     * Get the {@link KeyVaultRoleDefinition role definition} ID.
     *
     * @return The {@link KeyVaultRoleDefinition role definition} ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Get the {@link KeyVaultRoleDefinition role definition} name.
     *
     * @return The {@link KeyVaultRoleDefinition role definition} name.
     */
    public String getName() {
        return name;
    }

    /**
     * Get the {@link KeyVaultRoleDefinitionType role definition type}.
     *
     * @return The {@link KeyVaultRoleDefinitionType role definition type}.
     */
    public KeyVaultRoleDefinitionType getType() {
        return type;
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
