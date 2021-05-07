// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration.models;

import com.azure.core.annotation.Immutable;

/**
 * A class that defines a role.
 */
@Immutable
public final class KeyVaultRoleDefinition {
    private final String id;
    private final String name;
    private final KeyVaultRoleDefinitionType roleDefinitionType;
    private final KeyVaultRoleDefinitionProperties properties;

    /**
     * Creates a new {@link KeyVaultRoleDefinition role definition} with the specified details.
     *
     * @param id The ID for this {@link KeyVaultRoleDefinition role definition}.
     * @param name The name for this {@link KeyVaultRoleDefinition role definition}.
     * @param roleDefinitionType The roleDefinitionType for this {@link KeyVaultRoleDefinition role definition}.
     * @param properties {@link KeyVaultRoleDefinitionProperties properties} of this {@link KeyVaultRoleDefinition
     * role definition}.
     */
    public KeyVaultRoleDefinition(String id, String name, KeyVaultRoleDefinitionType roleDefinitionType,
                                  KeyVaultRoleDefinitionProperties properties) {
        this.id = id;
        this.name = name;
        this.roleDefinitionType = roleDefinitionType;
        this.properties = properties;
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
    public KeyVaultRoleDefinitionType getRoleDefinitionType() {
        return roleDefinitionType;
    }

    /**
     * Get the {@link KeyVaultRoleDefinitionProperties role definition properties}.
     *
     * @return The {@link KeyVaultRoleDefinitionProperties role definition properties}.
     */
    public KeyVaultRoleDefinitionProperties getProperties() {
        return properties;
    }
}
