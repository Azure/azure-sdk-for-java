// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.models;

import java.util.List;

/**
 * This class contains properties that are gettable and settable for path access control.
 */
public class PathAccessControl {

    private final List<PathAccessControlEntry> accessControlList;
    private final String group;
    private final String owner;
    private final PathPermissions permissions;

    /**
     * Constructs a new {@link PathAccessControl}.
     * @param accessControlList A list of {@link PathAccessControlEntry}
     * @param permissions {@link PathPermissions}
     * @param group The group
     * @param owner The owner
     */
    public PathAccessControl(List<PathAccessControlEntry> accessControlList, PathPermissions permissions,
        String group, String owner) {
        this.accessControlList = accessControlList;
        this.permissions = permissions;
        this.group = group;
        this.owner = owner;
    }

    /**
     * Get the accessControlList property: The accessControlList property.
     *
     * @return the accessControlList value.
     */
    public List<PathAccessControlEntry> getAccessControlList() {
        return accessControlList;
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
     * Get the owner property: The owner property.
     *
     * @return the owner value.
     */
    public String getOwner() {
        return owner;
    }

    /**
     * Get the permissions property: The permissions property.
     *
     * @return the permissions value.
     */
    public PathPermissions getPermissions() {
        return permissions;
    }
}
