// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.options;

import com.azure.core.annotation.Fluent;
import com.azure.storage.file.datalake.models.PathAccessControlEntry;
import com.azure.storage.file.datalake.models.DataLakeRequestConditions;
import com.azure.storage.file.datalake.models.PathHttpHeaders;

import java.util.List;
import java.util.Map;

/**
 * Extended options that may be passed when creating a datalake resource.
 */
@Fluent
public class DataLakePathCreateOptions {

    private String permissions;
    private String umask;
    private List<PathAccessControlEntry> accessControlEntryList;
    private String owner;
    private String group;
    private DataLakePathScheduleDeletionOptions deletionOptions;
    private PathHttpHeaders headers;
    private Map<String, String> metadata;
    private DataLakeRequestConditions requestConditions;
    private String sourceLeaseId;
    private String proposedLeaseId;
    private Integer leaseDuration;

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
    public DataLakePathCreateOptions setAccessControlList(List<PathAccessControlEntry> accessControl) {
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
    public DataLakePathCreateOptions setOwner(String owner) {
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
    public DataLakePathCreateOptions setGroup(String group) {
        this.group = group;
        return this;
    }

    /**
     * @return the {@link DataLakePathScheduleDeletionOptions} set on the path.
     */
    public DataLakePathScheduleDeletionOptions getScheduleDeletionOptions() {
        return deletionOptions;
    }

    /**
     * Scheduled deletion options to set on the path.
     * @param deletionOptions the {@link DataLakePathScheduleDeletionOptions} to set.
     * @return the updated options.
     */
    public DataLakePathCreateOptions setScheduleDeletionOptions(DataLakePathScheduleDeletionOptions deletionOptions) {
        this.deletionOptions = deletionOptions;
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

    /**
     * @return the source lease ID
     */
    public String getSourceLeaseId() {
        return sourceLeaseId;
    }

    /**
     * Sets the source lease ID.
     * @param leaseId the source lease ID.
     * @return The updated options.
     */
    public DataLakePathCreateOptions setSourceLeaseId(String leaseId) {
        sourceLeaseId = leaseId;
        return this;
    }

    /**
     * @return the proposed lease ID.
     */
    public String getProposedLeaseId() {
        return proposedLeaseId;
    }

    /**
     * Optional. Sets proposed lease ID.
     * Does not apply to directories.
     *
     * @param leaseId the proposed lease ID.
     * @return The updated options.
     */
    public DataLakePathCreateOptions setProposedLeaseId(String leaseId) {
        proposedLeaseId = leaseId;
        return this;
    }

    /**
     * @return the lease duration in seconds.
     */
    public Integer getLeaseDuration() {
        return leaseDuration;
    }

    /**
     * Optional.  Specifies the duration of the lease, in seconds, or specify -1 for a lease that never expires.
     * A non-infinite lease can be between 15 and 60 seconds.
     * Does not apply to directories.
     *
     * Sets the lease duration.
     * @param duration the new duration.
     * @return The updated options.
     */
    public DataLakePathCreateOptions setLeaseDuration(Integer duration) {
        leaseDuration = duration;
        return this;
    }

}
