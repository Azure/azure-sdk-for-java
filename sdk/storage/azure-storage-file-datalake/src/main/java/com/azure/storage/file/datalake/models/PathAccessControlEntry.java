// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.models;

import com.azure.storage.common.implementation.StorageImplUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * POSIX access control rights on files and directories.
 * <p>
 * The value is a comma-separated list of access control entries, each access control entry (ACE) consists of four
 * elements in the format "[scope:][type]:[id]:[permissions]":
 * <ul>
 *   <li>Scope</li>
 *   <li>Type</li>
 *   <li>User or Group Identifier (AAD ObjectId) </li>
 *   <li>Permissions</li>
 * </ul>
 * <p>
 * The scope must be "default" to indicate the ACE belongs to the default Access Control List (ACL) for a directory;
 * otherwise scope is implicit and the ACE belongs to the access ACL.
 * <p>
 * There are four ACE types:
 * <ul>
 *   <li>"user": grants rights to the owner or a named user</li>
 *   <li>"group" grants rights to the owning group or a named group</li>
 *   <li>"mask" restricts rights granted to named users and the members of groups</li>
 *   <li>"other" grants rights to all users not found in any of the other entries</li>
 * </ul>
 * <p>
 * The user or group identifier is omitted for entries of type "mask" and "other".
 * The user or group identifier is also omitted for the owner and owning group.
 * <p>
 * The permission field is a 3-character sequence where the first character is 'r' to grant read access, the second
 * character is 'w' to grant write access, and the third character is 'x' to grant execute permission.
 * If access is not granted, the '-' character is used to denote that the permission is denied.
 * <p>
 * For example, the following ACL grants read, write, and execute
 * rights to the file owner and john.doe@contoso, the read right to the owning group, and nothing to everyone else:
 * "user::rwx,user:john.doe@contoso:rwx,group::r--,other::---,mask::rwx".
 */
public class PathAccessControlEntry {

    static final String ACCESS_CONTROL_ENTRY_INVALID_SCOPE = "Scope must be default or otherwise omitted";

    /**
     * The string to specify default scope for an Access Control Entry.
     */
    static final String DEFAULT_SCOPE = "default";

    /**
     * Indicates whether this entry belongs to the default ACL for a directory.
     */
    private boolean defaultScope;

    /**
     * Indicates which role this entry targets.
     */
    private AccessControlType accessControlType;

    /**
     * Specifies the entity for which this entry applies. This is an AAD ObjectId.
     *
     * Must be omitted for types mask or other. It must also be omitted when the user or group is the owner.
     */
    private String entityId;

    /**
     * Specifies the permissions granted to this entry.
     */
    private RolePermissions permissions;

    /**
     * Initializes an empty instance of {@code PathAccessControlEntry}. Constructs an empty instance of
     * {@link RolePermissions} for the permissions field.
     */
    public PathAccessControlEntry() {
        this.permissions = new RolePermissions();
        this.accessControlType = new AccessControlType();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PathAccessControlEntry that = (PathAccessControlEntry) o;

        if (defaultScope != that.defaultScope) {
            return false;
        }
        if (accessControlType != that.accessControlType) {
            return false;
        }
        if (!Objects.equals(entityId, that.entityId)) {
            return false;
        }
        return Objects.equals(permissions, that.permissions);
    }

    @Override
    public int hashCode() {
        int result = (defaultScope ? 1 : 0);
        result = 31 * result + (accessControlType != null ? accessControlType.hashCode() : 0);
        result = 31 * result + (entityId != null ? entityId.hashCode() : 0);
        result = 31 * result + (permissions != null ? permissions.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        StorageImplUtils.assertNotNull("accessControlType", this.accessControlType);
        StorageImplUtils.assertNotNull("permissions", this.permissions);

        StringBuilder sb = new StringBuilder();
        if (this.defaultScope) {
            sb.append("default:");
        }
        sb.append(accessControlType.toString().toLowerCase(Locale.ROOT));
        sb.append(':');
        sb.append(entityId == null ? "" : entityId);
        sb.append(':');
        sb.append(permissions.toSymbolic());

        return sb.toString();
    }

    /**
     * Parses the provided string into a {@code List&lt{@link PathAccessControlEntry}&gt}.
     *
     * Must be of the format "[scope:][type]:[id]:[permissions]".
     *
     * @param str The string representation of the ACL.
     * @return The deserialized list.
     * @throws IllegalArgumentException if the String provided does not match the format.
     */
    public static PathAccessControlEntry parse(String str) {
        PathAccessControlEntry res = new PathAccessControlEntry();
        String[] parts = str.split(":");
        int indexOffset = 0;

        StorageImplUtils.assertInBounds("parts.length", parts.length, 3, 4);

        if (parts.length == 4) {
            if (!parts[0].equals(DEFAULT_SCOPE)) {
                throw new IllegalArgumentException(ACCESS_CONTROL_ENTRY_INVALID_SCOPE);
            }
            res.defaultScope = true;
            indexOffset = 1;
        }
        res.accessControlType = AccessControlType.fromString(parts[indexOffset]);
        res.entityId = !parts[1 + indexOffset].equals("") ? parts[1 + indexOffset] : null;
        res.permissions = RolePermissions.parseSymbolic(parts[2 + indexOffset], false);
        return res;
    }

    /**
     * Converts the Access Control List to a {@code String}. The format is specified in the description of this type.
     *
     * @param acl The Access Control List to serialize.
     * @return A {@code String} representing the serialized Access Control List
     */
    public static String serializeList(List<PathAccessControlEntry> acl) {
        StringBuilder sb = new StringBuilder();
        for (PathAccessControlEntry entry : acl) {
            sb.append(entry.toString());
            sb.append(',');
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    /**
     * Deserializes an ACL to the format "user::rwx,user:john.doe@contoso:rwx,group::r--,other::---,mask=rwx"
     *
     * @param str The {@code String} representation of the ACL.
     * @return The ACL deserialized into a {@code java.util.List}
     */
    public static List<PathAccessControlEntry> parseList(String str) {
        String[] strs = str.split(",");
        List<PathAccessControlEntry> acl = new ArrayList<>(strs.length);
        for (String entry : strs) {
            acl.add(PathAccessControlEntry.parse(entry));
        }
        return acl;
    }

    /**
     * Returns whether this ACE is in the default scope.
     *
     * @return {@code true} if in the default scope and {@code false} otherwise.
     */
    public boolean isInDefaultScope() {
        return defaultScope;
    }

    /**
     * Returns the {@link AccessControlType} for this entry.
     *
     * @return The {@link AccessControlType} for this entry.
     */
    public AccessControlType getAccessControlType() {
        return accessControlType;
    }

    /**
     * The Azure AAD Object ID or User Principal Name that is associated with this entry.
     *
     * @return The entity for which this entry applies.
     */
    public String getEntityId() {
        return entityId;
    }

    /**
     * Returns the symbolic form of the permissions for this entry.
     *
     * @return The {@link RolePermissions} for this entry.
     */
    public RolePermissions getPermissions() {
        return permissions;
    }

    /**
     * Sets whether or not this entry is the default for a directory.
     *
     * @param defaultScope {@code true} to set as the default scope and {@code false} otherwise.
     * @return The updated PathAccessControlEntry object.
     */
    public PathAccessControlEntry setDefaultScope(boolean defaultScope) {
        this.defaultScope = defaultScope;
        return this;
    }

    /**
     * Sets the {@link AccessControlType} for this entry.
     *
     * @param accessControlType The {@link AccessControlType} for this entry.
     * @return The updated PathAccessControlEntry object.
     */
    public PathAccessControlEntry setAccessControlType(AccessControlType accessControlType) {
        this.accessControlType = accessControlType;
        return this;
    }

    /**
     * Sets the entity ID to which this entry will apply. Must be null if the type is {@link AccessControlType#MASK} or
     * {@link AccessControlType#OTHER} or if the user is the owner or the group is the owning group. Must be a valid
     * Azure AAD Object ID or User Principal Name.
     *
     * @param entityId The entity to which this entry will apply.
     * @return The updated PathAccessControlEntry object.
     */
    public PathAccessControlEntry setEntityId(String entityId) {
        this.entityId = entityId;
        return this;
    }

    /**
     * Sets the permissions for this entry.
     *
     * @param permissions {@link RolePermissions} to set for this entry.
     * @return The updated PathAccessControlEntry object.
     */
    public PathAccessControlEntry setPermissions(RolePermissions permissions) {
        this.permissions = permissions;
        return this;
    }
}
