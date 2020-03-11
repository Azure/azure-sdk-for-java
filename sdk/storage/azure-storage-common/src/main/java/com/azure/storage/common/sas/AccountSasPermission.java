// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.sas;

import com.azure.storage.common.implementation.Constants;

import java.util.Locale;

/**
 * This is a helper class to construct a string representing the permissions granted by an Account SAS. Setting a value
 * to true means that any SAS which uses these permissions will grant permissions for that operation. Once all the
 * values are set, this should be serialized with toString and set as the permissions field on
 * {@link AccountSasSignatureValues AccountSasSignatureValues}.
 *
 * <p>
 * It is possible to construct the permissions string without this class, but the order of the permissions is particular
 * and this class guarantees correctness.
 * </p>
 *
 * @see AccountSasSignatureValues
 * @see <a href="https://docs.microsoft.com/rest/api/storageservices/create-account-sas">Create account SAS</a>
 */
public final class AccountSasPermission {

    private boolean readPermission;

    private boolean addPermission;

    private boolean createPermission;

    private boolean writePermission;

    private boolean deletePermission;

    private boolean listPermission;

    private boolean updatePermission;

    private boolean processMessagesPermission;

    /**
     * Initializes an {@link AccountSasPermission} object with all fields set to false.
     */
    public AccountSasPermission() {
    }

    /**
     * Creates an {@link AccountSasPermission} from the specified permissions string. This method will throw an {@link
     * IllegalArgumentException} if it encounters a character that does not correspond to a valid permission.
     *
     * @param permString A {@code String} which represents the {@link AccountSasPermission}.
     *
     * @return An {@link AccountSasPermission} object generated from the given {@link String}.
     *
     * @throws IllegalArgumentException If {@code permString} contains a character other than r, w, d, l, a, c, u,
     *     or p.
     */
    public static AccountSasPermission parse(String permString) {
        AccountSasPermission permissions = new AccountSasPermission();

        for (int i = 0; i < permString.length(); i++) {
            char c = permString.charAt(i);
            switch (c) {
                case 'r':
                    permissions.readPermission = true;
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
                case 'a':
                    permissions.addPermission = true;
                    break;
                case 'c':
                    permissions.createPermission = true;
                    break;
                case 'u':
                    permissions.updatePermission = true;
                    break;
                case 'p':
                    permissions.processMessagesPermission = true;
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
     * @return the read permission status
     */
    public boolean hasReadPermission() {
        return readPermission;
    }

    /**
     * Sets the read permission status.
     *
     * @param hasReadPermission Permission status to set
     *
     * @return the updated AccountSasPermission object
     */
    public AccountSasPermission setReadPermission(boolean hasReadPermission) {
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
     *
     * @return the updated AccountSasPermission object
     */
    public AccountSasPermission setAddPermission(boolean hasAddPermission) {
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
     *
     * @return the updated AccountSasPermission object
     */
    public AccountSasPermission setCreatePermission(boolean hasCreatePermission) {
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
     *
     * @return the updated AccountSasPermission object
     */
    public AccountSasPermission setWritePermission(boolean hasWritePermission) {
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
     *
     * @return the updated AccountSasPermission object
     */
    public AccountSasPermission setDeletePermission(boolean hasDeletePermission) {
        this.deletePermission = hasDeletePermission;
        return this;
    }

    /**
     * @return the list permission status
     */
    public boolean hasListPermission() {
        return listPermission;
    }

    /**
     * Sets the list permission status. This permission grants the ability to list blob containers, blobs, shares,
     * directories, and files.
     *
     * @param hasListPermission Permission status to set
     *
     * @return the updated AccountSasPermission object
     */
    public AccountSasPermission setListPermission(boolean hasListPermission) {
        this.listPermission = hasListPermission;
        return this;
    }

    /**
     * Returns the update permission status, it allows the update of queue message and tables.
     *
     * @return the update permission status
     */
    public boolean hasUpdatePermission() {
        return updatePermission;
    }

    /**
     * Sets the update permission status, it allows the update of queue messages and tables.
     *
     * @param hasUpdatePermission Permission status to set
     *
     * @return the updated AccountSasPermission object
     */
    public AccountSasPermission setUpdatePermission(boolean hasUpdatePermission) {
        this.updatePermission = hasUpdatePermission;
        return this;
    }

    /**
     * Returns the process messages permission, this allows the retrieval and deletion of queue messages.
     *
     * @return the process messages permission status.
     */
    public boolean hasProcessMessages() {
        return processMessagesPermission;
    }

    /**
     * Sets the process messages permission, this allows the retrieval and deletion of queue messages.
     *
     * @param hasProcessMessagesPermission Permission status to set
     *
     * @return the updated AccountSasPermission object
     */
    public AccountSasPermission setProcessMessages(boolean hasProcessMessagesPermission) {
        this.processMessagesPermission = hasProcessMessagesPermission;
        return this;
    }

    /**
     * Converts the given permissions to a {@link String}. Using this method will guarantee the permissions are in an
     * order accepted by the service.
     *
     * @return A {@link String} which represents the {@link AccountSasPermission}.
     */
    @Override
    public String toString() {
        // The order of the characters should be as specified here to ensure correctness:
        // https://docs.microsoft.com/en-us/rest/api/storageservices/constructing-an-account-sas
        final StringBuilder builder = new StringBuilder();

        if (this.readPermission) {
            builder.append('r');
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

        if (this.addPermission) {
            builder.append('a');
        }

        if (this.createPermission) {
            builder.append('c');
        }

        if (this.updatePermission) {
            builder.append('u');
        }

        if (this.processMessagesPermission) {
            builder.append('p');
        }

        return builder.toString();
    }
}
