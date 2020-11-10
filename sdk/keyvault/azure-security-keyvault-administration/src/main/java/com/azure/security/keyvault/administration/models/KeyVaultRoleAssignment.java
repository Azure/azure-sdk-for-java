// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration.models;

/**
 * A class that defines a role assignment.
 */
public final class KeyVaultRoleAssignment {
    private final String id;
    private final String name;
    private final String type;
    private final KeyVaultRoleAssignmentProperties properties;
    private final KeyVaultRoleAssignmentScope scope;

    /**
     * Creates a new {@link KeyVaultRoleAssignment role assignment} with the specified details.
     *
     * @param id The ID for this {@link KeyVaultRoleAssignment role assignment}.
     * @param name The name of this {@link KeyVaultRoleAssignment role assignment}.
     * @param type The type of this {@link KeyVaultRoleAssignment role assignment}.
     * @param properties {@link KeyVaultRoleAssignmentProperties properties} of this {@link KeyVaultRoleAssignment
     * role assignment}.
     * @param roleScope The {@link KeyVaultRoleAssignmentScope scope} of this {@link KeyVaultRoleAssignment role
     * assignment}.
     */
    public KeyVaultRoleAssignment(String id, String name, String type, KeyVaultRoleAssignmentProperties properties, KeyVaultRoleAssignmentScope roleScope) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.properties = properties;
        this.scope = roleScope;
    }

    /**
     * Get the {@link KeyVaultRoleAssignment role assignment} ID.
     *
     * @return The {@link KeyVaultRoleAssignment role assignment} ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Get the {@link KeyVaultRoleAssignment role assignment} name.
     *
     * @return The {@link KeyVaultRoleAssignment role assignment} name.
     */
    public String getName() {
        return name;
    }

    /**
     * Get the {@link KeyVaultRoleAssignment role assignment} type.
     *
     * @return The {@link KeyVaultRoleAssignment role assignment} type.
     */
    public String getType() {
        return type;
    }

    /**
     * Get the {@link KeyVaultRoleAssignment role assignment} {@link KeyVaultRoleAssignmentProperties properties}.
     *
     * @return The {@link KeyVaultRoleAssignment role assignment} {@link KeyVaultRoleAssignmentProperties properties}.
     */
    public KeyVaultRoleAssignmentProperties getProperties() {
        return properties;
    }

    /**
     * Get the {@link KeyVaultRoleAssignment role assignment} {@link KeyVaultRoleAssignmentScope scope}.
     *
     * @return The {@link KeyVaultRoleAssignment role assignment} {@link KeyVaultRoleAssignmentScope scope}.
     */
    public KeyVaultRoleAssignmentScope getScope() {
        return scope;
    }
}
