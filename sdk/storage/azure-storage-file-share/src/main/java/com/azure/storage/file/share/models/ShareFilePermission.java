// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file.share.models;

import com.azure.storage.file.share.implementation.models.FilePermissionFormat;

/**
 * Share File Permission.
 */
public class ShareFilePermission {

    /**
     * Format of File Permission.
     */
    private FilePermissionFormat permissionFormat;

    /**
     * The File Permission itself, in SDDL or base64 encoded binary format.
     */
    private String permission;


    /**
     * Get the format of file permission.
     *
     * @return the format of file permission.
     */
    public FilePermissionFormat getPermissionFormat() {
        return permissionFormat;
    }

    public ShareFilePermission setPermissionFormat(FilePermissionFormat permissionFormat) {
        this.permissionFormat = permissionFormat;
        return this;
    }

    public String getPermission() {
        return permission;
    }

    public ShareFilePermission setPermission(String permission) {
        this.permission = permission;
        return this;
    }
}
