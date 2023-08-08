// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file.datalake.models;

import com.azure.storage.common.implementation.StorageImplUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.azure.storage.file.datalake.models.PathAccessControlEntry.ACCESS_CONTROL_ENTRY_INVALID_SCOPE;
import static com.azure.storage.file.datalake.models.PathAccessControlEntry.DEFAULT_SCOPE;

/**
 * Represents an access control in a file access control list for removal.
 */
public class PathRemoveAccessControlEntry {
    private boolean defaultScope;
    private AccessControlType accessControlType;
    private String entityId;

    /**
     * Initializes an empty instance of {@code PathRemoveAccessControlEntry}.
     */
    public PathRemoveAccessControlEntry() {
        this.accessControlType = new AccessControlType();
    }

    /**
     * Indicates whether this is the default entry for the ACL.
     *
     * @return Whether this is the default entry for the ACL.
     */
    public boolean isDefaultScope() {
        return defaultScope;
    }

    /**
     * Sets whether this is the default entry for the ACL.
     *
     * @param defaultScope Whether this is the default entry for the ACL.
     * @return The updated object.
     */
    public PathRemoveAccessControlEntry setDefaultScope(boolean defaultScope) {
        this.defaultScope = defaultScope;
        return this;
    }

    /**
     * Specifies which role this entry targets.
     *
     * @return Which role this entry targets.
     */
    public AccessControlType getAccessControlType() {
        return accessControlType;
    }

    /**
     * Specifies which role this entry targets.
     *
     * @param accessControlType Which role this entry targets.
     * @return The updated object.
     */
    public PathRemoveAccessControlEntry setAccessControlType(AccessControlType accessControlType) {
        this.accessControlType = accessControlType;
        return this;
    }

    /**
     * Specifies the entity for which this entry applies.
     * Must be omitted for types mask or other.  It must also be omitted when the user or group is the owner.
     *
     * @return The entity for which this entry applies.
     */
    public String getEntityId() {
        return entityId;
    }

    /**
     * Specifies the entity for which this entry applies.
     * Must be omitted for types mask or other.  It must also be omitted when the user or group is the owner.
     *
     * @param entityId The entity for which this entry applies.
     * @return The updated object.
     */
    public PathRemoveAccessControlEntry setEntityId(String entityId) {
        this.entityId = entityId;
        return this;
    }


    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        if (this.defaultScope) {
            builder.append(DEFAULT_SCOPE);
            builder.append(":");
        }
        builder.append(accessControlType.toString().toLowerCase(Locale.ROOT));
        builder.append(':');
        builder.append(entityId == null ? "" : entityId);

        return builder.toString();
    }

    /**
     * Parses the provided string into a {@code PathAccessControlEntry}.
     *
     * Must be of the format "[scope:][type]:[id]".
     *
     * @param str The string representation of the ACL.
     * @return The deserialized list.
     * @throws IllegalArgumentException if the String provided does not match the format.
     */
    public static PathRemoveAccessControlEntry parse(String str) {
        PathRemoveAccessControlEntry res = new PathRemoveAccessControlEntry();
        String[] parts = str.split(":");
        int indexOffset = 0;

        StorageImplUtils.assertInBounds("parts.length", parts.length, 1, 3);

        if (parts.length == 3) {
            if (!parts[0].equals(DEFAULT_SCOPE)) {
                throw new IllegalArgumentException(ACCESS_CONTROL_ENTRY_INVALID_SCOPE);
            }
            res.defaultScope = true;
            indexOffset = 1;
        }
        res.accessControlType = AccessControlType.fromString(parts[indexOffset]);
        res.entityId = ((1 + indexOffset) < parts.length) && !parts[1 + indexOffset].equals("") ? parts[1 + indexOffset] : null;
        return res;
    }

    /**
     * Converts the Access Control List to a {@code String}. The format is specified in the description of this type.
     *
     * @param acl The Access Control List to serialize.
     * @return A {@code String} representing the serialized Access Control List
     */
    public static String serializeList(List<PathRemoveAccessControlEntry> acl) {
        StringBuilder sb = new StringBuilder();
        for (PathRemoveAccessControlEntry entry : acl) {
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
    public static List<PathRemoveAccessControlEntry> parseList(String str) {
        String[] strs = str.split(",");
        List<PathRemoveAccessControlEntry> acl = new ArrayList<>(strs.length);
        for (String entry : strs) {
            acl.add(PathRemoveAccessControlEntry.parse(entry));
        }
        return acl;
    }
}
