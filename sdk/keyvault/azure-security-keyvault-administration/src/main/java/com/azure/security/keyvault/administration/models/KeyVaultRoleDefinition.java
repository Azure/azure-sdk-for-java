// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration.models;

/**
 * A class that defines a role.
 */
public final class KeyVaultRoleDefinition {
    private final String id;
    private final String name;
    private final String type;
    private final KeyVaultRoleDefinitionProperties properties;

    /**
     * Creates a new {@link KeyVaultRoleDefinition role definition} with the specified details.
     *
     * @param id The ID for this {@link KeyVaultRoleDefinition role definition}.
     * @param name The name for this {@link KeyVaultRoleDefinition role definition}.
     * @param type The type for this {@link KeyVaultRoleDefinition role definition}.
     * @param properties {@link KeyVaultRoleDefinitionProperties properties} of this {@link KeyVaultRoleDefinition
     * role assignment}.
     */
    public KeyVaultRoleDefinition(String id, String name, String type, KeyVaultRoleDefinitionProperties properties) {
        this.id = id;
        this.name = name;
        this.type = type;
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
     * Get the {@link KeyVaultRoleDefinition role assignment} type.
     *
     * @return The {@link KeyVaultRoleDefinition role assignment} type.
     */
    public String getType() {
        return type;
    }

    /**
     * Get the {@link KeyVaultRoleDefinition role definition} {@link KeyVaultRoleDefinitionProperties properties}.
     *
     * @return The {@link KeyVaultRoleDefinition role assignment} {@link KeyVaultRoleDefinitionProperties properties}.
     */
    public KeyVaultRoleDefinitionProperties getProperties() {
        return properties;
    }
}
