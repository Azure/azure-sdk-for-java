// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.sas;


import com.azure.storage.common.implementation.Constants;

import java.util.Locale;

/**
 * This is a helper class to construct a string representing the permissions granted by a ServiceSAS to a container.
 * Setting a value to true means that any SAS which uses these permissions will grant permissions for that operation.
 * It is possible to construct the permissions string without this class, but the order of the permissions is
 * particular and this class guarantees correctness.
 */
public final class BlobContainerSasPermission {
    private boolean readPermission;

    private boolean addPermission;

    private boolean createPermission;

    private boolean writePermission;

    private boolean deletePermission;

    private boolean deleteVersionPermission;

    private boolean listPermission;

    private boolean tagsPermission;

    private boolean movePermission;

    private boolean executePermission;

    private boolean immutabilityPolicyPermission;

    /**
     * Initializes an {@code BlobContainerSasPermission} object with all fields set to false.
     */
    public BlobContainerSasPermission() {
    }

    /**
     * Creates an {@code BlobContainerSasPermission} from the specified permissions string. This method will throw an
     * {@code IllegalArgumentException} if it encounters a character that does not correspond to a valid permission.
     *
     * @param permissionString A {@code String} which represents the {@code BlobContainerSasPermission}.
     * @return A {@code BlobContainerSasPermission} generated from the given {@code String}.
     * @throws IllegalArgumentException If {@code permString} contains a character other than r, a, c, w, d, x, l, t or
     * i.
     */
    public static BlobContainerSasPermission parse(String permissionString) {
        BlobContainerSasPermission permissions = new BlobContainerSasPermission();

        for (int i = 0; i < permissionString.length(); i++) {
            char c = permissionString.charAt(i);
            switch (c) {
                case 'r':
                    permissions.readPermission = true;
                    break;
                case 'a':
                    permissions.addPermission = true;
                    break;
                case 'c':
                    permissions.createPermission = true;
                    break;
                case 'w':
                    permissions.writePermission = true;
                    break;
                case 'd':
                    permissions.deletePermission = true;
                    break;
                case 'x':
                    permissions.deleteVersionPermission = true;
                    break;
                case 'l':
                    permissions.listPermission = true;
                    break;
                case 't':
                    permissions.tagsPermission = true;
                    break;
                case 'm':
                    permissions.movePermission = true;
                    break;
                case 'e':
                    permissions.executePermission = true;
                    break;
                case 'i':
                    permissions.immutabilityPolicyPermission = true;
                    break;
                default:
                    throw new IllegalArgumentException(
                        String.format(Locale.ROOT, Constants.ENUM_COULD_NOT_BE_PARSED_INVALID_VALUE,
                            "Permissions", permissionString, c));
            }
        }
        return permissions;
    }

    /**
     * @return the read permission status
     */
    public boolean hasReadPermission() {
        return readPermission;
    }

    /**
     * Sets the read permission status.
     *
     * @param hasReadPermission Permission status to set
     * @return the updated BlobContainerSasPermission object
     */
    public BlobContainerSasPermission setReadPermission(boolean hasReadPermission) {
        this.readPermission = hasReadPermission;
        return this;
    }

    /**
     * @return the add permission status
     */
    public boolean hasAddPermission() {
        return addPermission;
    }

    /**
     * Sets the add permission status.
     *
     * @param hasAddPermission Permission status to set
     * @return the updated BlobContainerSasPermission object
     */
    public BlobContainerSasPermission setAddPermission(boolean hasAddPermission) {
        this.addPermission = hasAddPermission;
        return this;
    }

    /**
     * @return the create permission status
     */
    public boolean hasCreatePermission() {
        return createPermission;
    }

    /**
     * Sets the create permission status.
     *
     * @param hasCreatePermission Permission status to set
     * @return the updated BlobContainerSasPermission object
     */
    public BlobContainerSasPermission setCreatePermission(boolean hasCreatePermission) {
        this.createPermission = hasCreatePermission;
        return this;
    }

    /**
     * @return the write permission status
     */
    public boolean hasWritePermission() {
        return writePermission;
    }

    /**
     * Sets the write permission status.
     *
     * @param hasWritePermission Permission status to set
     * @return the updated BlobContainerSasPermission object
     */
    public BlobContainerSasPermission setWritePermission(boolean hasWritePermission) {
        this.writePermission = hasWritePermission;
        return this;
    }

    /**
     * @return the delete permission status
     */
    public boolean hasDeletePermission() {
        return deletePermission;
    }

    /**
     * Sets the delete permission status.
     *
     * @param hasDeletePermission Permission status to set
     * @return the updated BlobContainerSasPermission object
     */
    public BlobContainerSasPermission setDeletePermission(boolean hasDeletePermission) {
        this.deletePermission = hasDeletePermission;
        return this;
    }

    /**
     * @return the delete version permission status
     */
    public boolean hasDeleteVersionPermission() {
        return deleteVersionPermission;
    }

    /**
     * Sets the delete version permission status.
     *
     * @param hasDeleteVersionPermission Permission status to set
     * @return the updated BlobContainerSasPermission object
     */
    public BlobContainerSasPermission setDeleteVersionPermission(boolean hasDeleteVersionPermission) {
        this.deleteVersionPermission = hasDeleteVersionPermission;
        return this;
    }

    /**
     * @return the list permission status
     */
    public boolean hasListPermission() {
        return listPermission;
    }

    /**
     * Sets the list permission status.
     *
     * @param hasListPermission Permission status to set
     * @return the updated BlobContainerSasPermission object
     */
    public BlobContainerSasPermission setListPermission(boolean hasListPermission) {
        this.listPermission = hasListPermission;
        return this;
    }

    /**
     * @return the tags permission status.
     */
    public boolean hasTagsPermission() {
        return tagsPermission;
    }

    /**
     * Sets the tags permission status.
     *
     * @param tagsPermission Permission status to set
     * @return the updated BlobContainerSasPermission object.
     */
    public BlobContainerSasPermission setTagsPermission(boolean tagsPermission) {
        this.tagsPermission = tagsPermission;
        return this;
    }

    /**
     * @return the move permission status.
     */
    public boolean hasMovePermission() {
        return movePermission;
    }

    /**
     * Sets the move permission status.
     *
     * @param hasMovePermission Permission status to set
     * @return the updated BlobContainerSasPermission object.
     */
    public BlobContainerSasPermission setMovePermission(boolean hasMovePermission) {
        this.movePermission = hasMovePermission;
        return this;
    }

    /**
     * @return the execute permission status.
     */
    public boolean hasExecutePermission() {
        return executePermission;
    }

    /**
     * Sets the execute permission status.
     *
     * @param hasExecutePermission Permission status to set
     * @return the updated BlobContainerSasPermission object.
     */
    public BlobContainerSasPermission setExecutePermission(boolean hasExecutePermission) {
        this.executePermission = hasExecutePermission;
        return this;
    }

    /**
     * @return the set immutability policy permission status.
     */
    public boolean hasImmutabilityPolicyPermission() {
        return immutabilityPolicyPermission;
    }

    /**
     * Sets the set immutability policy permission status.
     *
     * @param immutabilityPolicyPermission Permission status to set
     * @return the updated BlobSasPermission object.
     */
    public BlobContainerSasPermission setImmutabilityPolicyPermission(boolean immutabilityPolicyPermission) {
        this.immutabilityPolicyPermission = immutabilityPolicyPermission;
        return this;
    }

    /**
     * Converts the given permissions to a {@code String}. Using this method will guarantee the permissions are in an
     * order accepted by the service.
     *
     * @return A {@code String} which represents the {@code BlobContainerSasPermission}.
     */
    @Override
    public String toString() {
        // The order of the characters should be as specified here to ensure correctness:
        // https://docs.microsoft.com/en-us/rest/api/storageservices/constructing-a-service-sas
        final StringBuilder builder = new StringBuilder();

        if (this.readPermission) {
            builder.append('r');
        }

        if (this.addPermission) {
            builder.append('a');
        }

        if (this.createPermission) {
            builder.append('c');
        }

        if (this.writePermission) {
            builder.append('w');
        }

        if (this.deletePermission) {
            builder.append('d');
        }

        if (this.deleteVersionPermission) {
            builder.append('x');
        }

        if (this.listPermission) {
            builder.append('l');
        }

        if (this.tagsPermission) {
            builder.append('t');
        }

        if (this.movePermission) {
            builder.append('m');
        }

        if (this.executePermission) {
            builder.append('e');
        }

        if (this.immutabilityPolicyPermission) {
            builder.append('i');
        }

        return builder.toString();
    }
}
