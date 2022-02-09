// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables.sas;

import com.azure.core.annotation.Fluent;
import com.azure.data.tables.implementation.StorageConstants;

import java.util.Locale;

/**
 * This is a helper class to construct a string representing the permissions granted by an Account SAS. Setting a value
 * to true means that any SAS which uses these permissions will grant permissions for that operation. Once all the
 * values are set, this should be serialized with {@code toString()} and set as the permissions field on an
 * {@link TableAccountSasSignatureValues} object.
 *
 * <p>
 * It is possible to construct the permissions string without this class, but the order of the permissions is particular
 * and this class guarantees correctness.
 * </p>
 *
 * @see TableAccountSasSignatureValues
 * @see <a href="https://docs.microsoft.com/rest/api/storageservices/create-account-sas">Create account SAS</a>
 */
@Fluent
public final class TableAccountSasPermission {
    private boolean readPermission;
    private boolean addPermission;
    private boolean createPermission;
    private boolean writePermission;
    private boolean deletePermission;
    private boolean deleteVersionPermission;
    private boolean listPermission;
    private boolean updatePermission;
    private boolean processMessagesPermission;
    private boolean tagsPermission;
    private boolean filterTagsPermission;

    /**
     * Creates an {@link TableAccountSasPermission} from the specified permissions string. This method will throw an
     * {@link IllegalArgumentException} if it encounters a character that does not correspond to a valid permission.
     *
     * @param permissionsString A {@code String} which represents the {@link TableAccountSasPermission account permissions}.
     *
     * @return An {@link TableAccountSasPermission} object generated from the given {@code String}.
     *
     * @throws IllegalArgumentException If {@code permString} contains a character other than r, w, d, x, l, a, c, u, p,
     * t or f.
     */
    public static TableAccountSasPermission parse(String permissionsString) {
        TableAccountSasPermission permissions = new TableAccountSasPermission();

        for (int i = 0; i < permissionsString.length(); i++) {
            char c = permissionsString.charAt(i);
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
                case 'x':
                    permissions.deleteVersionPermission = true;
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
                case 't':
                    permissions.tagsPermission = true;
                    break;
                case 'f':
                    permissions.filterTagsPermission = true;
                    break;
                default:
                    throw new IllegalArgumentException(
                        String.format(Locale.ROOT, StorageConstants.ENUM_COULD_NOT_BE_PARSED_INVALID_VALUE,
                            "Permissions", permissionsString, c));
            }
        }

        return permissions;
    }

    /**
     * Gets the read permission status. Valid for all signed resources types (Service, Container, and Object). Permits
     * read permissions to the specified resource type.
     *
     * @return The read permission status.
     */
    public boolean hasReadPermission() {
        return readPermission;
    }

    /**
     * Sets the read permission status. Valid for all signed resources types (Service, Container, and Object). Permits
     * read permissions to the specified resource type.
     *
     * @param hasReadPermission The permission status to set.
     *
     * @return The updated {@link TableAccountSasPermission} object.
     */
    public TableAccountSasPermission setReadPermission(boolean hasReadPermission) {
        this.readPermission = hasReadPermission;

        return this;
    }

    /**
     * Gets the add permission status. Valid for the following Object resource types only: queue messages, table
     * entities, and append blobs.
     *
     * @return The add permission status.
     */
    public boolean hasAddPermission() {
        return addPermission;
    }

    /**
     * Sets the add permission status. Valid for the following Object resource types only: queue messages, table
     * entities, and append blobs.
     *
     * @param hasAddPermission The permission status to set.
     *
     * @return The updated {@link TableAccountSasPermission} object.
     */
    public TableAccountSasPermission setAddPermission(boolean hasAddPermission) {
        this.addPermission = hasAddPermission;

        return this;
    }

    /**
     * Gets the create permission status. Valid for the following Object resource types only: blobs and files. Users can
     * create new blobs or files, but may not overwrite existing blobs or files.
     *
     * @return The create permission status.
     */
    public boolean hasCreatePermission() {
        return createPermission;
    }

    /**
     * Sets the create permission status. Valid for the following Object resource types only: blobs and files. Users can
     * create new blobs or files, but may not overwrite existing blobs or files.
     *
     * @param hasCreatePermission The permission status to set.
     *
     * @return The updated {@link TableAccountSasPermission} object.
     */
    public TableAccountSasPermission setCreatePermission(boolean hasCreatePermission) {
        this.createPermission = hasCreatePermission;

        return this;
    }

    /**
     * Gets the write permission status. Valid for all signed resources types (Service, Container, and Object). Permits
     * write permissions to the specified resource type.
     *
     * @return The write permission status.
     */
    public boolean hasWritePermission() {
        return writePermission;
    }

    /**
     * Sets the write permission status. Valid for all signed resources types (Service, Container, and Object). Permits
     * write permissions to the specified resource type.
     *
     * @param hasWritePermission The permission status to set.
     *
     * @return The updated {@link TableAccountSasPermission} object.
     */
    public TableAccountSasPermission setWritePermission(boolean hasWritePermission) {
        this.writePermission = hasWritePermission;

        return this;
    }

    /**
     * Gets the delete permission status. Valid for Container and Object resource types, except for queue messages.
     *
     * @return The delete permission status.
     */
    public boolean hasDeletePermission() {
        return deletePermission;
    }

    /**
     * Sets the delete permission status. Valid for Container and Object resource types, except for queue messages.
     *
     * @param hasDeletePermission The permission status to set.
     *
     * @return The updated {@link TableAccountSasPermission} object.
     */
    public TableAccountSasPermission setDeletePermission(boolean hasDeletePermission) {
        this.deletePermission = hasDeletePermission;

        return this;
    }

    /**
     * Gets the delete version permission status. Used to delete a blob version
     *
     * @return The delete version permission status.
     */
    public boolean hasDeleteVersionPermission() {
        return deleteVersionPermission;
    }

    /**
     * Sets the delete version permission status. Used to delete a blob version
     *
     * @param hasDeleteVersionPermission The permission status to set.
     *
     * @return The updated {@link TableAccountSasPermission} object.
     */
    public TableAccountSasPermission setDeleteVersionPermission(boolean hasDeleteVersionPermission) {
        this.deleteVersionPermission = hasDeleteVersionPermission;

        return this;
    }

    /**
     * Gets the list permission status. Valid for Service and Container resource types only.
     *
     * @return The list permission status.
     */
    public boolean hasListPermission() {
        return listPermission;
    }

    /**
     * Sets the list permission status. Valid for Service and Container resource types only.
     *
     * @param hasListPermission The permission status to set.
     *
     * @return The updated {@link TableAccountSasPermission} object.
     */
    public TableAccountSasPermission setListPermission(boolean hasListPermission) {
        this.listPermission = hasListPermission;

        return this;
    }

    /**
     * Gets the update permission status. Valid for the following Object resource types only: queue messages and table
     * entities.
     *
     * @return The update permission status.
     */
    public boolean hasUpdatePermission() {
        return updatePermission;
    }

    /**
     * Sets the update permission status. Valid for the following Object resource types only: queue messages and table
     * entities.
     *
     * @param hasUpdatePermission The permission status to set.
     *
     * @return The updated {@link TableAccountSasPermission} object.
     */
    public TableAccountSasPermission setUpdatePermission(boolean hasUpdatePermission) {
        this.updatePermission = hasUpdatePermission;

        return this;
    }

    /**
     * Gets the process messages permission. Valid for the following Object resource type only: queue messages.
     *
     * @return The process messages permission status.
     */
    public boolean hasProcessMessages() {
        return processMessagesPermission;
    }

    /**
     * Sets the process messages permission. Valid for the following Object resource type only: queue messages.
     *
     * @param hasProcessMessagesPermission The permission status to set.
     *
     * @return The updated {@link TableAccountSasPermission} object.
     */
    public TableAccountSasPermission setProcessMessages(boolean hasProcessMessagesPermission) {
        this.processMessagesPermission = hasProcessMessagesPermission;

        return this;
    }

    /**
     * @return The tags permission status. Used to read or write the tags on a blob.
     */
    public boolean hasTagsPermission() {
        return tagsPermission;
    }

    /**
     * Sets the tags permission status.
     *
     * @param tagsPermission The permission status to set. Used to read or write the tags on a blob.
     *
     * @return The updated {@link TableAccountSasPermission} object.
     */
    public TableAccountSasPermission setTagsPermission(boolean tagsPermission) {
        this.tagsPermission = tagsPermission;

        return this;
    }


    /**
     * @return The filter tags permission status. Used to filter blobs by their tags.
     */
    public boolean hasFilterTagsPermission() {
        return filterTagsPermission;
    }

    /**
     * Sets the filter tags permission status. Used to filter blobs by their tags.
     *
     * @param filterTagsPermission The permission status to set.
     *
     * @return The updated {@link TableAccountSasPermission} object.
     */
    public TableAccountSasPermission setFilterTagsPermission(boolean filterTagsPermission) {
        this.filterTagsPermission = filterTagsPermission;

        return this;
    }

    /**
     * Converts the given permissions to a {@code String}. Using this method will guarantee the permissions are in an
     * order accepted by the service. If all permissions are set to false, an empty string is returned from this method.
     *
     * @return A {@code String} which represents the {@link TableAccountSasPermission}.
     */
    @Override
    public String toString() {
        // The order of the characters should be as specified here to ensure correctness:
        // https://docs.microsoft.com/rest/api/storageservices/constructing-an-account-sas
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

        if (this.deleteVersionPermission) {
            builder.append('x');
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

        if (this.tagsPermission) {
            builder.append('t');
        }

        if (this.filterTagsPermission) {
            builder.append('f');
        }

        return builder.toString();
    }
}
