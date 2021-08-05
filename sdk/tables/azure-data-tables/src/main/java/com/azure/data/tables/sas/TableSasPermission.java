// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables.sas;

import com.azure.core.annotation.Fluent;
import com.azure.data.tables.implementation.StorageConstants;

import java.util.Locale;

/**
 * Constructs a string representing the permissions granted by an Azure Service SAS to a table. Setting a value to true
 * means that any SAS which uses these permissions will grant permissions for that operation. Once all the values are
 * set, this should be serialized with {@link #toString() toString} and set as the permissions field on
 * {@link TableSasSignatureValues#setPermissions(TableSasPermission)} TableSasSignatureValues}.
 *
 * <p>
 * It is possible to construct the permissions string without this class, but the order of the permissions is
 * particular and this class guarantees correctness.
 * </p>
 *
 * @see <a href="https://docs.microsoft.com/rest/api/storageservices/create-service-sas#permissions-for-a-table">
 * Permissions for a table</a>
 * @see TableSasSignatureValues
 */
@Fluent
public final class TableSasPermission {
    private boolean readPermission;
    private boolean addPermission;
    private boolean updatePermission;
    private boolean deletePermission;

    /**
     * Creates a {@link TableSasPermission} from the specified permissions string. This method will throw an
     * {@link IllegalArgumentException} if it encounters a character that does not correspond to a valid permission.
     *
     * @param permString A {@code String} which represents the {@link TableSasPermission}.
     *
     * @return A {@link TableSasPermission} generated from the given {@code String}.
     *
     * @throws IllegalArgumentException If {@code permString} contains a character other than r, a, u, or d.
     */
    public static TableSasPermission parse(String permString) {
        TableSasPermission permissions = new TableSasPermission();

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
                case 'd':
                    permissions.deletePermission = true;

                    break;
                default:
                    throw new IllegalArgumentException(
                        String.format(Locale.ROOT, StorageConstants.ENUM_COULD_NOT_BE_PARSED_INVALID_VALUE,
                            "Permissions", permString, c));
            }
        }

        return permissions;
    }

    /**
     * Gets the read permissions status.
     *
     * @return {@code true} if the SAS has permission to get entities and query entities. {@code false}, otherwise.
     */
    public boolean hasReadPermission() {
        return readPermission;
    }

    /**
     * Sets the read permission status.
     *
     * @param hasReadPermission {@code true} if the SAS has permission to get entities and query entities.
     * {@code false}, otherwise
     *
     * @return The updated TableSasPermission object.
     */
    public TableSasPermission setReadPermission(boolean hasReadPermission) {
        this.readPermission = hasReadPermission;

        return this;
    }

    /**
     * Gets the add permission status.
     *
     * @return {@code true} if the SAS has permission to add entities to the table. {@code false}, otherwise.
     */
    public boolean hasAddPermission() {
        return addPermission;
    }

    /**
     * Sets the add permission status.
     *
     * @param hasAddPermission {@code true} if the SAS has permission to add entities to the table. {@code false},
     * otherwise.
     *
     * <p>
     * <b>Note:</b> The {@code add} and {@code update} permissions are required for upsert operations.
     * </p>
     *
     * @return The updated {@link TableSasPermission} object.
     */
    public TableSasPermission setAddPermission(boolean hasAddPermission) {
        this.addPermission = hasAddPermission;

        return this;
    }

    /**
     * Gets the update permission status.
     *
     * @return {@code true} if the SAS has permission to update entities in the table. {@code false}, otherwise.
     */
    public boolean hasUpdatePermission() {
        return updatePermission;
    }

    /**
     * Sets the update permission status.
     *
     * <p>
     * <b>Note:</b> The {@code add} and {@code update} permissions are required for upsert operations.
     * </p>
     *
     * @param hasUpdatePermission {@code true} if the SAS has permission to update entities in the table. {@code false},
     * otherwise.
     *
     * @return The updated {@link TableSasPermission} object.
     */
    public TableSasPermission setUpdatePermission(boolean hasUpdatePermission) {
        this.updatePermission = hasUpdatePermission;

        return this;
    }

    /**
     * Gets the delete permission status.
     *
     * @return {@code true} if the SAS has permission to delete entities from the table. {@code false}, otherwise.
     */
    public boolean hasDeletePermission() {
        return deletePermission;
    }

    /**
     * Sets the process permission status.
     *
     * @param hasDeletePermission {@code true} if the SAS has permission to delete entities from the table.
     * {@code false}, otherwise.
     *
     * @return The updated {@link TableSasPermission} object.
     */
    public TableSasPermission setDeletePermission(boolean hasDeletePermission) {
        this.deletePermission = hasDeletePermission;

        return this;
    }

    /**
     * Converts the given permissions to a {@code String}. Using this method will guarantee the permissions are in an
     * order accepted by the service. If all permissions are set to false, an empty string is returned from this method.
     *
     * @return A {@code String} which represents the {@link TableSasPermission}.
     */
    @Override
    public String toString() {
        // The order of the characters should be as specified here to ensure correctness:
        // https://docs.microsoft.com/rest/api/storageservices/constructing-a-service-sas

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

        if (this.deletePermission) {
            builder.append('d');
        }

        return builder.toString();
    }
}
