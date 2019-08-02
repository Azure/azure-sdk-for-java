// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.storage.blob;

import java.util.Locale;

/**
 * This is a helper class to construct a string representing the permissions granted by a ServiceSAS to a blob. Setting
 * a value to true means that any SAS which uses these permissions will grant permissions for that operation. Once all
 * the values are set, this should be serialized with toString and set as the permissions field on a
 * {@link ServiceSASSignatureValues} object. It is possible to construct the permissions string without this class, but
 * the order of the permissions is particular and this class guarantees correctness.
 */
public final class BlobSASPermission {

    private boolean read;

    private boolean add;

    private boolean create;

    private boolean write;

    private boolean delete;

    /**
     * Initializes a {@code BlobSASPermission} object with all fields set to false.
     */
    public BlobSASPermission() {
    }

    /**
     * Creates a {@code BlobSASPermission} from the specified permissions string. This method will throw an
     * {@code IllegalArgumentException} if it encounters a character that does not correspond to a valid permission.
     *
     * @param permString
     *         A {@code String} which represents the {@code BlobSASPermission}.
     *
     * @return A {@code BlobSASPermission} generated from the given {@code String}.
     */
    public static BlobSASPermission parse(String permString) {
        BlobSASPermission permissions = new BlobSASPermission();

        for (int i = 0; i < permString.length(); i++) {
            char c = permString.charAt(i);
            switch (c) {
                case 'r':
                    permissions.read = true;
                    break;
                case 'a':
                    permissions.add = true;
                    break;
                case 'c':
                    permissions.create = true;
                    break;
                case 'w':
                    permissions.write = true;
                    break;
                case 'd':
                    permissions.delete = true;
                    break;
                default:
                    throw new IllegalArgumentException(
                            String.format(Locale.ROOT, SR.ENUM_COULD_NOT_BE_PARSED_INVALID_VALUE, "Permissions", permString, c));
            }
        }
        return permissions;
    }

    /**
     * Specifies Read access granted.
     */
    public boolean read() {
        return read;
    }

    /**
     * Specifies Read access granted.
     */
    public BlobSASPermission withRead(boolean read) {
        this.read = read;
        return this;
    }

    /**
     * Specifies Add access granted.
     */
    public boolean add() {
        return add;
    }

    /**
     * Specifies Add access granted.
     */
    public BlobSASPermission withAdd(boolean add) {
        this.add = add;
        return this;
    }

    /**
     * Specifies Create access granted.
     */
    public boolean create() {
        return create;
    }

    /**
     * Specifies Create access granted.
     */
    public BlobSASPermission withCreate(boolean create) {
        this.create = create;
        return this;
    }

    /**
     * Specifies Write access granted.
     */
    public boolean write() {
        return write;
    }

    /**
     * Specifies Write access granted.
     */
    public BlobSASPermission withWrite(boolean write) {
        this.write = write;
        return this;
    }

    /**
     * Specifies Delete access granted.
     */
    public boolean delete() {
        return delete;
    }

    /**
     * Specifies Delete access granted.
     */
    public BlobSASPermission withDelete(boolean delete) {
        this.delete = delete;
        return this;
    }

    /**
     * Converts the given permissions to a {@code String}. Using this method will guarantee the permissions are in an
     * order accepted by the service.
     *
     * @return A {@code String} which represents the {@code BlobSASPermission}.
     */
    @Override
    public String toString() {
        // The order of the characters should be as specified here to ensure correctness:
        // https://docs.microsoft.com/en-us/rest/api/storageservices/constructing-a-service-sas

        final StringBuilder builder = new StringBuilder();

        if (this.read) {
            builder.append('r');
        }

        if (this.add) {
            builder.append('a');
        }

        if (this.create) {
            builder.append('c');
        }

        if (this.write) {
            builder.append('w');
        }

        if (this.delete) {
            builder.append('d');
        }

        return builder.toString();
    }
}
