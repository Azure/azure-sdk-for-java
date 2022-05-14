// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.options;

import com.azure.core.annotation.Fluent;
import com.azure.storage.file.datalake.models.PathExpiryOptions;
import com.azure.storage.file.datalake.models.DataLakeRequestConditions;
import com.azure.storage.file.datalake.models.PathAccessControlEntry;
import com.azure.storage.file.datalake.models.PathHttpHeaders;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
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
    private List<PathAccessControlEntry> accessControlEntryList;
    private String owner;
    private String group;
    private String sourceLeaseId;
    private String proposedLeaseId;
    private Long leaseDuration;
    private PathExpiryOptions expiryOptions;
    private OffsetDateTime expiresOn;
    private Duration timeToExpire;

    /**
     * Constructs a {@link DataLakePathCreateOptions}.
     */
    public DataLakePathCreateOptions() {
    }

    /**
     * Gets the permissions.
     *
     * @return the permissions
     */
    public String getPermissions() {
        return permissions;
    }

    /**
     * Sets the permissions.
     *
     * @param permissions The permissions.
     * @return the updated options.
     */
    public DataLakePathCreateOptions setPermissions(String permissions) {
        this.permissions = permissions;
        return this;
    }

    /**
     * Gets the umask.
     *
     * @return the umask.
     */
    public String getUmask() {
        return umask;
    }

    /**
     * Sets the umask.
     *
     * @param umask The umask.
     * @return the updated options.
     */
    public DataLakePathCreateOptions setUmask(String umask) {
        this.umask = umask;
        return this;
    }

    /**
     * Gets the http headers.
     *
     * @return the http headers.
     */
    public PathHttpHeaders getPathHttpHeaders() {
        return headers;
    }

    /**
     * Sets the umask.
     *
     * @param headers The http headers.
     * @return the updated options.
     */
    public DataLakePathCreateOptions setPathHttpHeaders(PathHttpHeaders headers) {
        this.headers = headers;
        return this;
    }

    /**
     * @return Metadata to associate with the datalake path.
     */
    public Map<String, String> getMetadata() {
        return metadata;
    }

    /**
     * @param metadata Metadata to associate with the datalake path. If there is leading or trailing whitespace in any
     * metadata key or value, it must be removed or encoded.
     * @return The updated options.
     */
    public DataLakePathCreateOptions setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }

    /**
     * Gets the request conditions.
     *
     * @return the request conditions.
     */
    public DataLakeRequestConditions getRequestConditions() {
        return requestConditions;
    }

    /**
     * Sets the request conditions.
     *
     * @param requestConditions The request conditions.
     * @return the updated FileQueryOptions object.
     */
    public DataLakePathCreateOptions setRequestConditions(DataLakeRequestConditions requestConditions) {
        this.requestConditions = requestConditions;
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
    public Long getLeaseDuration() {
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
    public DataLakePathCreateOptions setLeaseDuration(Long duration) {
        leaseDuration = duration;
        return this;
    }

    /**
     * @return the expiry options.
     */
    public PathExpiryOptions getExpiryOptions() {
        return expiryOptions;
    }

    /**
     * Sets the expiry options.
     * @param options the new expiry options.
     * @return The updated options.
     */
    public DataLakePathCreateOptions setExpiryOptions(PathExpiryOptions options) {
        expiryOptions = options;
        return this;
    }

    /**
     * @return the expiry date.
     */
    public OffsetDateTime getExpiresOn() {
        return expiresOn;
    }

    /**
     * Sets the expiry date.
     * @param expiresOn sets the expiry date.
     * @return The updated options.
     */
    public DataLakePathCreateOptions setExpiresOn(OffsetDateTime expiresOn) {
        this.expiresOn = expiresOn;
        return this;
    }

    /**
     * @return the time to expire.
     */
    public Duration getTimeToExpire() {
        return timeToExpire;
    }

    /**
     * Sets the expiry date.
     * @param expiryTime sets the expiry date.
     * @return The updated options.
     */
    public DataLakePathCreateOptions setTimeToExpire(Duration expiryTime) {
        timeToExpire = expiryTime;
        return this;
    }

}
