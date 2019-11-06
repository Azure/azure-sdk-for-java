// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.queue.sas;

import com.azure.storage.common.implementation.Constants;

import java.util.Locale;

/**
 * Constructs a string representing the permissions granted by an Azure Service SAS to a queue. Setting a value to true
 * means that any SAS which uses these permissions will grant permissions for that operation. Once all the values are
 * set, this should be serialized with {@link #toString() toString} and set as the permissions field on
 * {@link QueueServiceSasSignatureValues#setPermissions(QueueSasPermission) QueueServiceSasSignatureValues}.
 *
 * <p>
 * It is possible to construct the permissions string without this class, but the order of the permissions is
 * particular and this class guarantees correctness.
 * </p>
 *
 * @see <a href="https://docs.microsoft.com/rest/api/storageservices/create-service-sas#permissions-for-a-queue>
 *     Permissions for a queue</a>
 * @see QueueServiceSasSignatureValues
 */
public final class QueueSasPermission {

    private boolean readPermission;

    private boolean addPermission;

    private boolean updatePermission;

    private boolean processPermission;

    /**
     * Initializes a {@link QueueSasPermission} object with all fields set to false.
     */
    public QueueSasPermission() {
    }

    /**
     * Creates a {@link QueueSasPermission} from the specified permissions string. This method will throw an
     * {@link IllegalArgumentException} if it encounters a character that does not correspond to a valid permission.
     *
     * @param permString A {@code String} which represents the {@code QueueSasPermission}.
     * @return A {@code QueueSasPermission} generated from the given {@code String}.
     * @throws IllegalArgumentException If {@code permString} contains a character other than r, a, u, or p.
     */
    public static QueueSasPermission parse(String permString) {
        QueueSasPermission permissions = new QueueSasPermission();

        for (int i = 0; i < permString.length(); i++) {
            char c = permString.charAt(i);
            switch (c) {
                case 'r':
                    permissions.readPermission = true;
                    break;
                case 'a':
                    permissions.addPermission = true;
                    break;
                case 'u':
                    permissions.updatePermission = true;
                    break;
                case 'p':
                    permissions.processPermission = true;
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
     * Gets the read permissions status.
     *
     * @return {@code true} if SAS has permission to read metadata, properties, message count, peek at messages.
     * {@code false}, otherwise.
     */
    public boolean hasReadPermission() {
        return readPermission;
    }

    /**
     * Sets the read permission status.
     *
     * @param hasReadPermission {@code true} if SAS has permission to read metadata, properties, message count, peek at
     * messages. {@code false}, otherwise.
     * @return The updated QueueSasPermission object.
     */
    public QueueSasPermission setReadPermission(boolean hasReadPermission) {
        this.readPermission = hasReadPermission;
        return this;
    }

    /**
     * Gets the add permission status.
     *
     * @return {@code true} if SAS has permission to add messages to the queue. {@code false}, otherwise.
     */
    public boolean hasAddPermission() {
        return addPermission;
    }

    /**
     * Sets the add permission status.
     *
     * @param hasAddPermission {@code true} if SAS has permission to add messages to the queue. {@code false},
     * otherwise.
     * @return the updated QueueSasPermission object.
     */
    public QueueSasPermission setAddPermission(boolean hasAddPermission) {
        this.addPermission = hasAddPermission;
        return this;
    }

    /**
     * Gets the update permission status.
     *
     * @return {@code true} if SAS has permission to update messages in the queue. {@code false}, otherwise.
     */
    public boolean hasUpdatePermission() {
        return updatePermission;
    }

    /**
     * Sets the update permission status.
     *
     *  <p>
     * <b>Note:</b> Use {@link #setProcessPermission(boolean) setProcessPermission(true)} to get a particular message in
     * the queue to update.
     * </p>
     *
     * @param hasUpdatePermission {@code true} if SAS has permission to update messages in the queue. {@code false},
     * otherwise.
     * @return the updated QueueSasPermission object.
     */
    public QueueSasPermission setUpdatePermission(boolean hasUpdatePermission) {
        this.updatePermission = hasUpdatePermission;
        return this;
    }

    /**
     * Gets the process permission status.
     *
     * @return {@code true} if SAS has permission to get and delete messages from the queue. {@code false}, otherwise.
     */
    public boolean hasProcessPermission() {
        return processPermission;
    }

    /**
     * Sets the process permission status.
     *
     * @param hasProcessPermission {@code true} if SAS has permission to get and delete messages from the queue.
     * {@code false}, otherwise.
     * @return the updated QueueSasPermission object.
     */
    public QueueSasPermission setProcessPermission(boolean hasProcessPermission) {
        this.processPermission = hasProcessPermission;
        return this;
    }

    /**
     * Converts the given permissions to a {@link String}. Using this method will guarantee the permissions are in an
     * order accepted by the service.
     *
     * @return A {@link String} which represents the {@link QueueSasPermission}.
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

        if (this.updatePermission) {
            builder.append('u');
        }

        if (this.processPermission) {
            builder.append('p');
        }

        return builder.toString();
    }
}
