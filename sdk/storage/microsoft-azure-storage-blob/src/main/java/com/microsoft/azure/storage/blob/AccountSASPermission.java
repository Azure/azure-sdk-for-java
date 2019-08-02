// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.storage.blob;

import java.util.Locale;

/**
 * This is a helper class to construct a string representing the permissions granted by an AccountSAS. Setting a value
 * to true means that any SAS which uses these permissions will grant permissions for that operation. Once all the
 * values are set, this should be serialized with toString and set as the permissions field on an
 * {@link AccountSASSignatureValues} object. It is possible to construct the permissions string without this class, but
 * the order of the permissions is particular and this class guarantees correctness.
 */
public final class AccountSASPermission {

    private boolean read;

    private boolean add;

    private boolean create;

    private boolean write;

    private boolean delete;

    private boolean list;

    private boolean update;

    private boolean processMessages;

    /**
     * Initializes an {@code AccountSASPermission} object with all fields set to false.
     */
    public AccountSASPermission() {
    }

    /**
     * Creates an {@code AccountSASPermission} from the specified permissions string. This method will throw an
     * {@code IllegalArgumentException} if it encounters a character that does not correspond to a valid permission.
     *
     * @param permString
     *         A {@code String} which represents the {@code SharedAccessAccountPermissions}.
     *
     * @return An {@code AccountSASPermission} object generated from the given {@code String}.
     */
    public static AccountSASPermission parse(String permString) {
        AccountSASPermission permissions = new AccountSASPermission();

        for (int i = 0; i < permString.length(); i++) {
            char c = permString.charAt(i);
            switch (c) {
                case 'r':
                    permissions.read = true;
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
                case 'a':
                    permissions.add = true;
                    break;
                case 'c':
                    permissions.create = true;
                    break;
                case 'u':
                    permissions.update = true;
                    break;
                case 'p':
                    permissions.processMessages = true;
                    break;
                default:
                    throw new IllegalArgumentException(
                            String.format(Locale.ROOT, SR.ENUM_COULD_NOT_BE_PARSED_INVALID_VALUE, "Permissions", permString, c));
            }
        }
        return permissions;
    }

    /**
     * Permission to read resources and list queues and tables granted.
     */
    public boolean read() {
        return read;
    }

    /**
     * Permission to read resources and list queues and tables granted.
     */
    public AccountSASPermission withRead(boolean read) {
        this.read = read;
        return this;
    }

    /**
     * Permission to add messages, table entities, and append to blobs granted.
     */
    public boolean add() {
        return add;
    }

    /**
     * Permission to add messages, table entities, and append to blobs granted.
     */
    public AccountSASPermission withAdd(boolean add) {
        this.add = add;
        return this;
    }

    /**
     * Permission to create blobs and files granted.
     */
    public boolean create() {
        return create;
    }

    /**
     * Permission to create blobs and files granted.
     */
    public AccountSASPermission withCreate(boolean create) {
        this.create = create;
        return this;
    }

    /**
     * Permission to write resources granted.
     */
    public boolean write() {
        return write;
    }

    /**
     * Permission to write resources granted.
     */
    public AccountSASPermission withWrite(boolean write) {
        this.write = write;
        return this;
    }

    /**
     * Permission to delete resources granted.
     */
    public boolean delete() {
        return delete;
    }

    /**
     * Permission to delete resources granted.
     */
    public AccountSASPermission withDelete(boolean delete) {
        this.delete = delete;
        return this;
    }

    /**
     * Permission to list blob containers, blobs, shares, directories, and files granted.
     */
    public boolean list() {
        return list;
    }

    /**
     * Permission to list blob containers, blobs, shares, directories, and files granted.
     */
    public AccountSASPermission withList(boolean list) {
        this.list = list;
        return this;
    }

    /**
     * Permissions to update messages and table entities granted.
     */
    public boolean update() {
        return update;
    }

    /**
     * Permissions to update messages and table entities granted.
     */
    public AccountSASPermission withUpdate(boolean update) {
        this.update = update;
        return this;
    }

    /**
     * Permission to get and delete messages granted.
     */
    public boolean processMessages() {
        return processMessages;
    }

    /**
     * Permission to get and delete messages granted.
     */
    public AccountSASPermission withProcessMessages(boolean processMessages) {
        this.processMessages = processMessages;
        return this;
    }

    /**
     * Converts the given permissions to a {@code String}. Using this method will guarantee the permissions are in an
     * order accepted by the service.
     *
     * @return A {@code String} which represents the {@code AccountSASPermissions}.
     */
    @Override
    public String toString() {
        // The order of the characters should be as specified here to ensure correctness:
        // https://docs.microsoft.com/en-us/rest/api/storageservices/constructing-an-account-sas
        final StringBuilder builder = new StringBuilder();

        if (this.read) {
            builder.append('r');
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

        if (this.add) {
            builder.append('a');
        }

        if (this.create) {
            builder.append('c');
        }

        if (this.update) {
            builder.append('u');
        }

        if (this.processMessages) {
            builder.append('p');
        }

        return builder.toString();
    }
}
