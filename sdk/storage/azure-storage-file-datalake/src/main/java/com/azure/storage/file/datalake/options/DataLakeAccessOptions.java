// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.options;

import com.azure.core.annotation.Fluent;
import com.azure.storage.file.datalake.models.PathAccessControlEntry;

import java.util.List;

/**
 * Extended access options that may be passed to set when creating a path.
 */
@Fluent
public class DataLakeAccessOptions {

    private String permissions;
    private String umask;
    private List<PathAccessControlEntry> accessControlEntryList;
    private String owner;
    private String group;

    /**
     * Optional parameters for accessing a file or directory.
     */
    public DataLakeAccessOptions() {
    }

    /**
     * Optional and only valid if Hierarchical Namespace is enabled for the account.
     *
     * @return the permissions
     */
    public String getPermissions() {
        return permissions;
    }

    /**
     * Optional and only valid if Hierarchical Namespace is enabled for the account. Sets POSIX access
     * permissions for the file owner, the file owning group, and others. Each class may be granted read,
     * write, or execute permission. The sticky bit is also supported. Both symbolic (rwxrw-rw-) and 4-digit
     * octal notation (e.g. 0766) are supported.
     *
     * @param permissions The permissions.
     * @return the updated options.
     */
    public DataLakeAccessOptions setPermissions(String permissions) {
        this.permissions = permissions;
        return this;
    }

    /**
     * Optional and only valid if Hierarchical Namespace is enabled for the account.
     *
     * @return the umask.
     */
    public String getUmask() {
        return umask;
    }

    /**
     * Optional and only valid if Hierarchical Namespace is enabled for the account.
     * When creating a file or directory and the parent folder does not have a default ACL,
     * the umask restricts the permissions of the file or directory to be created. The resulting
     * permission is given by p bitwise-and ^u, where p is the permission and u is the umask. For example,
     * if p is 0777 and u is 0057, then the resulting permission is 0720. The default permission is
     * 0777 for a directory and 0666 for a file. The default umask is 0027. The umask must be specified
     * in 4-digit octal notation (e.g. 0766).
     *
     * @param umask The umask.
     * @return the updated options.
     */
    public DataLakeAccessOptions setUmask(String umask) {
        this.umask = umask;
        return this;
    }

    /**
     * @return the POSIX access control list for the file/directory.
     */
    public List<PathAccessControlEntry> getAccessControlList() {
        return accessControlEntryList;
    }

    /**
     * Optional. The POSIX access control list for the file or directory.
     *
     * @param accessControl The access control list.
     * @return The updated options.
     */
    public DataLakeAccessOptions setAccessControlList(List<PathAccessControlEntry> accessControl) {
        this.accessControlEntryList = accessControl;
        return this;
    }

    /**
     * @return the name of owner of the file/directory.
     */
    public String getOwner() {
        return owner;
    }

    /**
     * Optional. Sets the owner of the file/directory.
     * @param owner the new owner.
     * @return The updated options.
     */
    public DataLakeAccessOptions setOwner(String owner) {
        this.owner = owner;
        return this;
    }

    /**
     * @return the name of owning group of the file/directory.
     */
    public String getGroup() {
        return group;
    }

    /**
     * Optional. Sets the owning group of the file/directory.
     * @param group the new owning group.
     * @return The updated options.
     */
    public DataLakeAccessOptions setGroup(String group) {
        this.group = group;
        return this;
    }

}
