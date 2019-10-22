// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file;

import com.azure.storage.common.implementation.Constants;

import java.util.Locale;

/**
 * Constructs a string representing the permissions granted by a Service SAS to a file. Setting a value to true means
 * that any SAS which uses these permissions will grant permissions for that operation. Once all the values are set,
 * this should be serialized with {@link #toString() toString()} and set as the permissions field on
 * {@link FileServiceSasSignatureValues#setPermissions(FileSasPermission) FileServiceSasSignatureValues}.
 *
 * <p>
 * It is possible to construct the permissions string without this class, but the order of the permissions is particular
 * and this class guarantees correctness.
 * </p>
 *
 * @see FileServiceSasSignatureValues
 * @see <a href="https://docs.microsoft.com/rest/api/storageservices/create-service-sas#permissions-for-a-file>
 *     Permissions for a file</a>
 */
public final class FileSasPermission {
    private boolean readPermission;

    private boolean createPermission;

    private boolean writePermission;

    private boolean deletePermission;

    /**
     * Initializes an {@link FileSasPermission} object with all fields set to false.
     */
    public FileSasPermission() {
    }

    /**
     * Creates an {@code FileSasPermission} from the specified permissions string. This method will throw an
     * {@link IllegalArgumentException} if it encounters a character that does not correspond to a valid permission.
     *
     * @param permString A {@link String} which represents the {@link FileSasPermission}.
     * @return A {@link FileSasPermission} generated from the given {@link String}.
     * @throws IllegalArgumentException If {@code permString} contains a character other than r, c, w, or d.
     */
    public static FileSasPermission parse(String permString) {
        FileSasPermission permissions = new FileSasPermission();

        for (int i = 0; i < permString.length(); i++) {
            char c = permString.charAt(i);
            switch (c) {
                case 'r':
                    permissions.readPermission = true;
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
                default:
                    throw new IllegalArgumentException(
                        String.format(Locale.ROOT, Constants.ENUM_COULD_NOT_BE_PARSED_INVALID_VALUE,
                            "Permissions", permString, c));
            }
        }
        return permissions;
    }

    /**
     * Gets the read permission status.
     *
     * @return {@code true} if the SAS can read the content, properties, and metadata for a file. Can use the file as
     * the source of a copy operation. {@code false}, otherwise.
     */
    public boolean hasReadPermission() {
        return readPermission;
    }

    /**
     * Sets the read permission status.
     *
     * @param hasReadPermission {@code true} if the SAS can read the content, properties, and metadata for a file. Can
     * use the file as the source of a copy operation. {@code false}, otherwise.
     * @return the updated FileSasPermission object
     */
    public FileSasPermission setReadPermission(boolean hasReadPermission) {
        this.readPermission = hasReadPermission;
        return this;
    }

    /**
     * Gets the create permission status.
     *
     * @return {@code true} if SAS can create a new file or copy a file to a new file. {@code false}, otherwise.
     */
    public boolean hasCreatePermission() {
        return createPermission;
    }

    /**
     * Sets the create permission status.
     *
     * @param hasCreatePermission {@code true} if SAS can create a new file or copy a file to a new file. {@code false},
     * otherwise.
     * @return the updated FileSasPermission object
     */
    public FileSasPermission setCreatePermission(boolean hasCreatePermission) {
        this.createPermission = hasCreatePermission;
        return this;
    }

    /**
     * Gets the write permission status.
     *
     * @return {@code true} if SAS can write content, properties, or metadata to the file. Or, use the file as the
     * destination of a copy operation. {@code false}, otherwise.
     */
    public boolean hasWritePermission() {
        return writePermission;
    }

    /**
     * Sets the write permission status.
     *
     * @param hasWritePermission {@code true} if SAS can write content, properties, or metadata to the file. Or, use the
     * file as the destination of a copy operation. {@code false}, otherwise.
     * @return the updated FileSasPermission object
     */
    public FileSasPermission setWritePermission(boolean hasWritePermission) {
        this.writePermission = hasWritePermission;
        return this;
    }

    /**
     * Gets the delete permission status.
     *
     * @return {@code true} if SAS can delete a file. {@code false}, otherwise.
     */
    public boolean hasDeletePermission() {
        return deletePermission;
    }

    /**
     * Sets the delete permission status.
     *
     * @param hasDeletePermission {@code true} if SAS can delete a file. {@code false}, otherwise.
     * @return the updated FileSasPermission object
     */
    public FileSasPermission setDeletePermission(boolean hasDeletePermission) {
        this.deletePermission = hasDeletePermission;
        return this;
    }

    /**
     * Converts the given permissions to a {@link String}. Using this method will guarantee the permissions are in an
     * order accepted by the service.
     *
     * @return A {@link String} which represents the {@link FileSasPermission}.
     */
    @Override
    public String toString() {
        // The order of the characters should be as specified here to ensure correctness:
        // https://docs.microsoft.com/en-us/rest/api/storageservices/constructing-a-service-sas
        final StringBuilder builder = new StringBuilder();

        if (this.readPermission) {
            builder.append('r');
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

        return builder.toString();
    }
}
