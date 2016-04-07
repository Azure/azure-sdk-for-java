/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.models.implementation.api;

import java.util.List;

/**
 * Role definition properties.
 */
public class RoleDefinitionProperties {
    /**
     * Gets or sets role name.
     */
    private String roleName;

    /**
     * Gets or sets role definition description.
     */
    private String description;

    /**
     * Gets or sets role type.
     */
    private String type;

    /**
     * Gets or sets role definition permissions.
     */
    private List<PermissionInner> permissions;

    /**
     * Gets or sets role definition assignable scopes.
     */
    private List<String> assignableScopes;

    /**
     * Get the roleName value.
     *
     * @return the roleName value
     */
    public String roleName() {
        return this.roleName;
    }

    /**
     * Set the roleName value.
     *
     * @param roleName the roleName value to set
     * @return the RoleDefinitionProperties object itself.
     */
    public RoleDefinitionProperties setRoleName(String roleName) {
        this.roleName = roleName;
        return this;
    }

    /**
     * Get the description value.
     *
     * @return the description value
     */
    public String description() {
        return this.description;
    }

    /**
     * Set the description value.
     *
     * @param description the description value to set
     * @return the RoleDefinitionProperties object itself.
     */
    public RoleDefinitionProperties setDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Get the type value.
     *
     * @return the type value
     */
    public String type() {
        return this.type;
    }

    /**
     * Set the type value.
     *
     * @param type the type value to set
     * @return the RoleDefinitionProperties object itself.
     */
    public RoleDefinitionProperties setType(String type) {
        this.type = type;
        return this;
    }

    /**
     * Get the permissions value.
     *
     * @return the permissions value
     */
    public List<PermissionInner> permissions() {
        return this.permissions;
    }

    /**
     * Set the permissions value.
     *
     * @param permissions the permissions value to set
     * @return the RoleDefinitionProperties object itself.
     */
    public RoleDefinitionProperties setPermissions(List<PermissionInner> permissions) {
        this.permissions = permissions;
        return this;
    }

    /**
     * Get the assignableScopes value.
     *
     * @return the assignableScopes value
     */
    public List<String> assignableScopes() {
        return this.assignableScopes;
    }

    /**
     * Set the assignableScopes value.
     *
     * @param assignableScopes the assignableScopes value to set
     * @return the RoleDefinitionProperties object itself.
     */
    public RoleDefinitionProperties setAssignableScopes(List<String> assignableScopes) {
        this.assignableScopes = assignableScopes;
        return this;
    }

}
