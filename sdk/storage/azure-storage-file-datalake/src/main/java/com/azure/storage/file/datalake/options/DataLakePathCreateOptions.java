// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.options;

import com.azure.core.annotation.Fluent;
import com.azure.storage.file.datalake.models.DataLakeRequestConditions;
import com.azure.storage.file.datalake.models.PathHttpHeaders;
import java.util.Map;

/**
 * Extended options that may be passed when creating a datalake resource.
 */
@Fluent
public class DataLakePathCreateOptions {

    private String permissions;
    private String umask;
    private PathHttpHeaders headers;
    private Map<String, String> metadata;
    private DataLakeRequestConditions requestConditions;

    /**
     * Optional parameters for creating a file or directory.
     */
    public DataLakePathCreateOptions() {
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
    public DataLakePathCreateOptions setPermissions(String permissions) {
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
    public DataLakePathCreateOptions setUmask(String umask) {
        this.umask = umask;
        return this;
    }

    /**
     * Gets the http header properties.
     *
     * @return the http headers.
     */
    public PathHttpHeaders getPathHttpHeaders() {
        return headers;
    }

    /**
     * Optional standard HTTP header properties that can be set for the new file or directory.
     *
     * @param headers The http headers.
     * @return the updated options.
     */
    public DataLakePathCreateOptions setPathHttpHeaders(PathHttpHeaders headers) {
        this.headers = headers;
        return this;
    }

    /**
     * @return Metadata associated with the datalake path.
     */
    public Map<String, String> getMetadata() {
        return metadata;
    }

    /**
     * Optional custom metadata to set for this file or directory.
     *
     * @param metadata Metadata to associate with the datalake path. If there is leading or trailing whitespace in any
     * metadata key or value, it must be removed or encoded.
     * @return The updated options.
     */
    public DataLakePathCreateOptions setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }

    /**
     * Optional {@link DataLakeRequestConditions} conditions on the creation of this file or directory.
     *
     * @return the request conditions.
     */
    public DataLakeRequestConditions getRequestConditions() {
        return requestConditions;
    }

    /**
     * Optional {@link DataLakeRequestConditions} conditions on the creation of this file or directory.
     * Sets the request conditions.
     *
     * @param requestConditions The request conditions.
     * @return The updated options.
     */
    public DataLakePathCreateOptions setRequestConditions(DataLakeRequestConditions requestConditions) {
        this.requestConditions = requestConditions;
        return this;
    }

}
