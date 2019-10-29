// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.models;

/**
 * This class contains properties that are gettable and settable for path access control.
 */
public class PathAccessControl {

    private String acl;
    private String group;
    private String owner;
    private String permissions;

    /**
     * Constructs a new {@link PathAccessControl}.
     */
    public PathAccessControl() {

    }

    /**
     * Get the acl property: The acl property.
     *
     * @return the acl value.
     */
    public String getAcl() {
        return acl;
    }

    /**
     * Set the acl property: The acl property.
     *
     * @param acl the acl value to set.
     * @return the PathAccessControl object itself.
     */
    public PathAccessControl setAcl(String acl) {
        this.acl = acl;
        return this;
    }

    /**
     * Get the group property: The group property.
     *
     * @return the group value.
     */
    public String getGroup() {
        return group;
    }

    /**
     * Set the group property: The group property.
     *
     * @param group the group value to set.
     * @return the PathAccessControl object itself.
     */
    public PathAccessControl setGroup(String group) {
        this.group = group;
        return this;
    }

    /**
     * Get the owner property: The owner property.
     *
     * @return the owner value.
     */
    public String getOwner() {
        return owner;
    }

    /**
     * Set the owner property: The owner property.
     *
     * @param owner the owner value to set.
     * @return the PathAccessControl object itself.
     */
    public PathAccessControl setOwner(String owner) {
        this.owner = owner;
        return this;
    }

    /**
     * Get the permissions property: The permissions property.
     *
     * @return the permissions value.
     */
    public String getPermissions() {
        return permissions;
    }

    /**
     * Set the permission property: The permission property.
     *
     * @param permissions the permissions value to set.
     * @return the PathAccessControl object itself.
     */
    public PathAccessControl setPermissions(String permissions) {
        this.permissions = permissions;
        return this;
    }
}
