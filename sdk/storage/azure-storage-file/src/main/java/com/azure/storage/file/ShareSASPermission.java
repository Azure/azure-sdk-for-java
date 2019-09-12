// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file;


import com.azure.storage.common.SR;

import java.util.Locale;

/**
 * This is a helper class to construct a string representing the permissions granted by a ServiceSAS to a share. Setting
 * a value to true means that any SAS which uses these permissions will grant permissions for that operation. Once all
 * the values are set, this should be serialized with toString and set as the permissions field on a {@link
 * FileServiceSASSignatureValues} object. It is possible to construct the permissions string without this class, but the
 * order of the permissions is particular and this class guarantees correctness.
 */
public final class ShareSASPermission {
    private boolean read;

    private boolean create;

    private boolean write;

    private boolean delete;

    private boolean list;

    /**
     * Initializes an {@code ShareSASPermission} object with all fields set to false.
     */
    public ShareSASPermission() {
    }

    /**
     * Creates an {@code ShareSASPermission} from the specified permissions string. This method will throw an {@code
     * IllegalArgumentException} if it encounters a character that does not correspond to a valid permission.
     *
     * @param permString A {@code String} which represents the {@code ShareSASPermission}.
     * @return A {@code ShareSASPermission} generated from the given {@code String}.
     * @throws IllegalArgumentException If {@code permString} contains a character other than r, c, w, d, or l.
     */
    public static ShareSASPermission parse(String permString) {
        ShareSASPermission permissions = new ShareSASPermission();

        for (int i = 0; i < permString.length(); i++) {
            char c = permString.charAt(i);
            switch (c) {
                case 'r':
                    permissions.read = true;
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
                case 'l':
                    permissions.list = true;
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
    public boolean getRead() {
        return read;
    }

    /**
     * Sets the read permission status.
     *
     * @param read Permission status to set
     * @return the updated ShareSASPermission object
     */
    public ShareSASPermission setRead(boolean read) {
        this.read = read;
        return this;
    }

    /**
     * @return the create permission status
     */
    public boolean getCreate() {
        return create;
    }

    /**
     * Sets the create permission status.
     *
     * @param create Permission status to set
     * @return the updated ShareSASPermission object
     */
    public ShareSASPermission setCreate(boolean create) {
        this.create = create;
        return this;
    }

    /**
     * @return the write permission status
     */
    public boolean getWrite() {
        return write;
    }

    /**
     * Sets the write permission status.
     *
     * @param write Permission status to set
     * @return the updated ShareSASPermission object
     */
    public ShareSASPermission setWrite(boolean write) {
        this.write = write;
        return this;
    }

    /**
     * @return the delete permission status
     */
    public boolean getDelete() {
        return delete;
    }

    /**
     * Sets the delete permission status.
     *
     * @param delete Permission status to set
     * @return the updated ShareSASPermission object
     */
    public ShareSASPermission setDelete(boolean delete) {
        this.delete = delete;
        return this;
    }

    /**
     * @return the list permission status
     */
    public boolean getList() {
        return list;
    }

    /**
     * Sets the list permission status.
     *
     * @param list Permission status to set
     * @return the updated ShareSASPermission object
     */
    public ShareSASPermission setList(boolean list) {
        this.list = list;
        return this;
    }

    /**
     * Converts the given permissions to a {@code String}. Using this method will guarantee the permissions are in an
     * order accepted by the service.
     *
     * @return A {@code String} which represents the {@code ShareSASPermission}.
     */
    @Override
    public String toString() {
        // The order of the characters should be as specified here to ensure correctness:
        // https://docs.microsoft.com/en-us/rest/api/storageservices/constructing-a-service-sas
        final StringBuilder builder = new StringBuilder();

        if (this.read) {
            builder.append('r');
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

        if (this.list) {
            builder.append('l');
        }

        return builder.toString();
    }
}
