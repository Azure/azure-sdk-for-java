// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;


import com.azure.storage.common.SR;

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

    private boolean listPermission;

    /**
     * Initializes an {@code BlobContainerSasPermission} object with all fields set to false.
     */
    public BlobContainerSasPermission() {
    }

    /**
     * Creates an {@code BlobContainerSasPermission} from the specified permissions string. This method will throw an
     * {@code IllegalArgumentException} if it encounters a character that does not correspond to a valid permission.
     *
     * @param permString A {@code String} which represents the {@code BlobContainerSasPermission}.
     * @return A {@code BlobContainerSasPermission} generated from the given {@code String}.
     * @throws IllegalArgumentException If {@code permString} contains a character other than r, a, c, w, d, or l.
     */
    public static BlobContainerSasPermission parse(String permString) {
        BlobContainerSasPermission permissions = new BlobContainerSasPermission();

        for (int i = 0; i < permString.length(); i++) {
            char c = permString.charAt(i);
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
                case 'l':
                    permissions.listPermission = true;
                    break;
                default:
                    throw new IllegalArgumentException(
                        String.format(Locale.ROOT, SR.ENUM_COULD_NOT_BE_PARSED_INVALID_VALUE,
                            "Permissions", permString, c));
            }
        }
        return permissions;
    }

    /**
     * @return the read permission status
     */
    public boolean getReadPermission() {
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
    public boolean getAddPermission() {
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
    public boolean getCreatePermission() {
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
    public boolean getWritePermission() {
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
    public boolean getDeletePermission() {
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
     * @return the list permission status
     */
    public boolean getListPermission() {
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

        if (this.listPermission) {
            builder.append('l');
        }

        return builder.toString();
    }
}
