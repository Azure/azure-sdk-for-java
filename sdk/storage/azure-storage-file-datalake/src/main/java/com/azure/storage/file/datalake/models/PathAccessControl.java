// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.models;

import java.util.List;

/**
 * This class contains properties that are gettable and settable for path access control.
 */
public class PathAccessControl {

    private List<PathAccessControlEntry> accessControlList;
    private String group;
    private String owner;
    private PathPermissions permissions;

    /**
     * Constructs a new {@link PathAccessControl}.
     */
    public PathAccessControl() {

    }

    /**
     * Get the accessControlList property: The accessControlList property.
     *
     * @return the accessControlList value.
     */
    public List<PathAccessControlEntry> accessControlList() {
        return accessControlList;
    }

    /**
     * Set the accessControlList property: The accessControlList property.
     *
     * @param accessControlList the accessControlList value to set.
     * @return the PathAccessControl object itself.
     */
    public PathAccessControl accessControlList(List<PathAccessControlEntry> accessControlList) {
        this.accessControlList = accessControlList;
        return this;
    }

    /**
     * Get the group property: The group property.
     *
     * @return the group value.
     */
    public String group() {
        return group;
    }

    /**
     * Set the group property: The group property.
     *
     * @param group the group value to set.
     * @return the PathAccessControl object itself.
     */
    public PathAccessControl group(String group) {
        this.group = group;
        return this;
    }

    /**
     * Get the owner property: The owner property.
     *
     * @return the owner value.
     */
    public String owner() {
        return owner;
    }

    /**
     * Set the owner property: The owner property.
     *
     * @param owner the owner value to set.
     * @return the PathAccessControl object itself.
     */
    public PathAccessControl owner(String owner) {
        this.owner = owner;
        return this;
    }

    /**
     * Get the permissions property: The permissions property.
     *
     * @return the permissions value.
     */
    public PathPermissions permissions() {
        return permissions;
    }

    /**
     * Set the permission property: The permission property.
     *
     * @param permissions the permissions value to set.
     * @return the PathAccessControl object itself.
     */
    public PathAccessControl setPermissions(PathPermissions permissions) {
        this.permissions = permissions;
        return this;
    }
}
