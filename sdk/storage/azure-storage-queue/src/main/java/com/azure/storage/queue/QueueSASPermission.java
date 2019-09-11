// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.queue;

import com.azure.storage.common.SR;

import java.util.Locale;

/**
 * This is a helper class to construct a string representing the permissions granted by a ServiceSAS to a queue. Setting
 * a value to true means that any SAS which uses these permissions will grant permissions for that operation. Once all
 * the values are set, this should be serialized with toString and set as the permissions field on a
 * {@link QueueServiceSASSignatureValues} object. It is possible to construct the permissions string without this class, but
 * the order of the permissions is particular and this class guarantees correctness.
 */
public final class QueueSASPermission {

    private boolean read;

    private boolean add;

    private boolean update;

    private boolean process;

    /**
     * Initializes a {@code QueueSASPermission} object with all fields set to false.
     */
    public QueueSASPermission() {
    }

    /**
     * Creates a {@code QueueSASPermission} from the specified permissions string. This method will throw an
     * {@code IllegalArgumentException} if it encounters a character that does not correspond to a valid permission.
     *
     * @param permString A {@code String} which represents the {@code QueueSASPermission}.
     *
     * @return A {@code QueueSASPermission} generated from the given {@code String}.
     * @throws IllegalArgumentException If {@code permString} contains a character other than r, a, u, or p.
     */
    public static QueueSASPermission parse(String permString) {
        QueueSASPermission permissions = new QueueSASPermission();

        for (int i = 0; i < permString.length(); i++) {
            char c = permString.charAt(i);
            switch (c) {
                case 'r':
                    permissions.read = true;
                    break;
                case 'a':
                    permissions.add = true;
                    break;
                case 'u':
                    permissions.update = true;
                    break;
                case 'p':
                    permissions.process = true;
                    break;
                default:
                    throw new IllegalArgumentException(
                            String.format(Locale.ROOT, SR.ENUM_COULD_NOT_BE_PARSED_INVALID_VALUE, "Permissions", permString, c));
            }
        }
        return permissions;
    }

    /**
     * @return the read permission status.
     */
    public boolean getRead() {
        return read;
    }

    /**
     * Sets the read permission status.
     *
     * @param read Permission status to set
     * @return the updated QueueSASPermission object.
     */
    public QueueSASPermission setRead(boolean read) {
        this.read = read;
        return this;
    }

    /**
     * @return the add permission status.
     */
    public boolean getAdd() {
        return add;
    }

    /**
     * Sets the add permission status.
     *
     * @param add Permission status to set
     * @return the updated QueueSASPermission object.
     */
    public QueueSASPermission setAdd(boolean add) {
        this.add = add;
        return this;
    }

    /**
     * @return the update permission status.
     */
    public boolean getUpdate() {
        return update;
    }

    /**
     * Sets the update permission status.
     *
     * @param update Permission status to set
     * @return the updated QueueSASPermission object.
     */
    public QueueSASPermission setUpdate(boolean update) {
        this.update = update;
        return this;
    }
    /**
     * @return the process permission status.
     */
    public boolean getProcess() {
        return process;
    }

    /**
     * Sets the process permission status.
     *
     * @param process Permission status to set
     * @return the updated QueueSASPermission object.
     */
    public QueueSASPermission setProcess(boolean process) {
        this.process = process;
        return this;
    }

    /**
     * Converts the given permissions to a {@code String}. Using this method will guarantee the permissions are in an
     * order accepted by the service.
     *
     * @return A {@code String} which represents the {@code QueueSASPermission}.
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

        if (this.update) {
            builder.append('u');
        }

        if (this.process) {
            builder.append('p');
        }

        return builder.toString();
    }
}
