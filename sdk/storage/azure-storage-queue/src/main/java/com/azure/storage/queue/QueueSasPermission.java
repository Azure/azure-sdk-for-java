// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.queue;

import com.azure.storage.common.SR;

import java.util.Locale;

/**
 * This is a helper class to construct a string representing the permissions granted by a ServiceSAS to a queue. Setting
 * a value to true means that any SAS which uses these permissions will grant permissions for that operation. Once all
 * the values are set, this should be serialized with toString and set as the permissions field on a {@link
 * QueueServiceSasSignatureValues} object. It is possible to construct the permissions string without this class, but
 * the order of the permissions is particular and this class guarantees correctness.
 */
public final class QueueSasPermission {

    private boolean readPermission;

    private boolean addPermission;

    private boolean updatePermission;

    private boolean processPermission;

    /**
     * Initializes a {@code QueueSasPermission} object with all fields set to false.
     */
    public QueueSasPermission() {
    }

    /**
     * Creates a {@code QueueSasPermission} from the specified permissions string. This method will throw an
     * {@code IllegalArgumentException} if it encounters a character that does not correspond to a valid permission.
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
                        String.format(Locale.ROOT, SR.ENUM_COULD_NOT_BE_PARSED_INVALID_VALUE,
                            "Permissions", permString, c));
            }
        }
        return permissions;
    }

    /**
     * @return the read permission status.
     */
    public boolean getReadPermission() {
        return readPermission;
    }

    /**
     * Sets the read permission status.
     *
     * @param hadReadPermission Permission status to set
     * @return the updated QueueSasPermission object.
     */
    public QueueSasPermission setReadPermission(boolean hadReadPermission) {
        this.readPermission = hadReadPermission;
        return this;
    }

    /**
     * @return the add permission status.
     */
    public boolean getAddPermission() {
        return addPermission;
    }

    /**
     * Sets the add permission status.
     *
     * @param hasAddPermission Permission status to set
     * @return the updated QueueSasPermission object.
     */
    public QueueSasPermission setAddPermission(boolean hasAddPermission) {
        this.addPermission = hasAddPermission;
        return this;
    }

    /**
     * @return the update permission status.
     */
    public boolean getUpdatePermission() {
        return updatePermission;
    }

    /**
     * Sets the update permission status.
     *
     * @param hasUpdatePermission Permission status to set
     * @return the updated QueueSasPermission object.
     */
    public QueueSasPermission setUpdatePermission(boolean hasUpdatePermission) {
        this.updatePermission = hasUpdatePermission;
        return this;
    }

    /**
     * @return the process permission status.
     */
    public boolean getProcessPermission() {
        return processPermission;
    }

    /**
     * Sets the process permission status.
     *
     * @param hasProcessPermission Permission status to set
     * @return the updated QueueSasPermission object.
     */
    public QueueSasPermission setProcessPermission(boolean hasProcessPermission) {
        this.processPermission = hasProcessPermission;
        return this;
    }

    /**
     * Converts the given permissions to a {@code String}. Using this method will guarantee the permissions are in an
     * order accepted by the service.
     *
     * @return A {@code String} which represents the {@code QueueSasPermission}.
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
