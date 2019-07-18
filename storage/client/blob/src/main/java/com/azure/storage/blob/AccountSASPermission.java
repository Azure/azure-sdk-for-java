// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

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
     * @throws IllegalArgumentException If {@code permString} contains a character other than r, w, d, l, a, c, u, or p.
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
     * @return the read permission status
     */
    public boolean read() {
        return read;
    }

    /**
     * Sets the read permission status.
     *
     * @param read Permission status to set
     * @return the updated AccountSASPermission object
     */
    public AccountSASPermission read(boolean read) {
        this.read = read;
        return this;
    }

    /**
     * @return the add permission status
     */
    public boolean add() {
        return add;
    }

    /**
     * Sets the add permission status.
     *
     * @param add Permission status to set
     * @return the updated AccountSASPermission object
     */
    public AccountSASPermission add(boolean add) {
        this.add = add;
        return this;
    }

    /**
     * @return the create permission status
     */
    public boolean create() {
        return create;
    }

    /**
     * Sets the create permission status.
     *
     * @param create Permission status to set
     * @return the updated AccountSASPermission object
     */
    public AccountSASPermission create(boolean create) {
        this.create = create;
        return this;
    }

    /**
     * @return the write permission status
     */
    public boolean write() {
        return write;
    }

    /**
     * Sets the write permission status.
     *
     * @param write Permission status to set
     * @return the updated AccountSASPermission object
     */
    public AccountSASPermission write(boolean write) {
        this.write = write;
        return this;
    }

    /**
     * @return the delete permission status
     */
    public boolean delete() {
        return delete;
    }

    /**
     * Sets the delete permission status.
     *
     * @param delete Permission status to set
     * @return the updated AccountSASPermission object
     */
    public AccountSASPermission delete(boolean delete) {
        this.delete = delete;
        return this;
    }

    /**
     * @return the list permission status
     */
    public boolean list() {
        return list;
    }

    /**
     * Sets the list permission status. This permission grants the ability to list blob containers, blobs, shares,
     * directories, and files.
     *
     * @param list Permission status to set
     * @return the updated AccountSASPermission object
     */
    public AccountSASPermission list(boolean list) {
        this.list = list;
        return this;
    }

    /**
     * Returns the update permission status, it allows the update of queue message and tables.
     *
     * @return the update permission status
     */
    public boolean update() {
        return update;
    }

    /**
     * Sets the update permission status, it allows the update of queue messages and tables.
     *
     * @param update Permission status to set
     * @return the updated AccountSASPermission object
     */
    public AccountSASPermission update(boolean update) {
        this.update = update;
        return this;
    }

    /**
     * Returns the process messages permission, this allows the retrieval and deletion of queue messages.
     *
     * @return the process messages permission status.
     */
    public boolean processMessages() {
        return processMessages;
    }

    /**
     * Sets the process messages permission, this allows the retrieval and deletion of queue messages.
     *
     * @param processMessages Permission status to set
     * @return the updated AccountSASPermission object
     */
    public AccountSASPermission processMessages(boolean processMessages) {
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
