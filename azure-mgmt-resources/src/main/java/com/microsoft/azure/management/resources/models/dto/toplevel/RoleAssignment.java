/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.models.dto.toplevel;


import com.microsoft.azure.management.resources.models.dto.RoleAssignmentPropertiesWithScope;

/**
 * Role Assignments.
 */
public class RoleAssignment {
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
    public String getId() {
        return this.id;
    }

    /**
     * Set the id value.
     *
     * @param id the id value to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Get the name value.
     *
     * @return the name value
     */
    public String getName() {
        return this.name;
    }

    /**
     * Set the name value.
     *
     * @param name the name value to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the type value.
     *
     * @return the type value
     */
    public String getType() {
        return this.type;
    }

    /**
     * Set the type value.
     *
     * @param type the type value to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Get the properties value.
     *
     * @return the properties value
     */
    public RoleAssignmentPropertiesWithScope getProperties() {
        return this.properties;
    }

    /**
     * Set the properties value.
     *
     * @param properties the properties value to set
     */
    public void setProperties(RoleAssignmentPropertiesWithScope properties) {
        this.properties = properties;
    }

}
