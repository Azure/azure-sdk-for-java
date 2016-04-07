/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.models.implementation.api;


/**
 * Role Assignments.
 */
public class RoleAssignmentInner {
    /**
     * Gets or sets role assignment id.
     */
    private String id;

    /**
     * Gets or sets role assignment name.
     */
    private String name;

    /**
     * Gets or sets role assignment type.
     */
    private String type;

    /**
     * Gets or sets role assignment properties.
     */
    private RoleAssignmentPropertiesWithScope properties;

    /**
     * Get the id value.
     *
     * @return the id value
     */
    public String id() {
        return this.id;
    }

    /**
     * Set the id value.
     *
     * @param id the id value to set
     * @return the RoleAssignmentInner object itself.
     */
    public RoleAssignmentInner setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Get the name value.
     *
     * @return the name value
     */
    public String name() {
        return this.name;
    }

    /**
     * Set the name value.
     *
     * @param name the name value to set
     * @return the RoleAssignmentInner object itself.
     */
    public RoleAssignmentInner setName(String name) {
        this.name = name;
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
     * @return the RoleAssignmentInner object itself.
     */
    public RoleAssignmentInner setType(String type) {
        this.type = type;
        return this;
    }

    /**
     * Get the properties value.
     *
     * @return the properties value
     */
    public RoleAssignmentPropertiesWithScope properties() {
        return this.properties;
    }

    /**
     * Set the properties value.
     *
     * @param properties the properties value to set
     * @return the RoleAssignmentInner object itself.
     */
    public RoleAssignmentInner setProperties(RoleAssignmentPropertiesWithScope properties) {
        this.properties = properties;
        return this;
    }

}
