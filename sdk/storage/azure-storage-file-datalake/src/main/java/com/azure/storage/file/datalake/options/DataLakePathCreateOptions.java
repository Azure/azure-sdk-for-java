// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.options;

import com.azure.core.annotation.Fluent;
import com.azure.storage.file.datalake.models.PathExpiryMode;
import com.azure.storage.file.datalake.models.DataLakeRequestConditions;
import com.azure.storage.file.datalake.models.PathHttpHeaders;

import java.util.Map;

/**
 * Extended options that may be passed when creating a datalake resource.
 */
@Fluent
public class DataLakePathCreateOptions {

    private DataLakeAccessOptions accessOptions;
    private DataLakePathScheduleDeletionOptions deletionOptions;
    private PathHttpHeaders headers;
    private Map<String, String> metadata;
    private DataLakeRequestConditions requestConditions;
    private String sourceLeaseId;
    private String proposedLeaseId;
    private int leaseDuration;
    private PathExpiryMode expiryOptions;

    /**
     * Optional parameters for creating a file or directory.
     */
    public DataLakePathCreateOptions() {
    }

    /**
     * @return the {@link DataLakeAccessOptions} set on the path.
     */
    public DataLakeAccessOptions getAccessOptions() {
        return accessOptions;
    }

    /**
     * Access options to set on the newly-created path.
     * @param accessOptions the {@link DataLakeAccessOptions} to set.
     * @return the updated options.
     */
    public DataLakePathCreateOptions setAccessOptions(DataLakeAccessOptions accessOptions) {
        this.accessOptions = accessOptions;
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
    public int getLeaseDuration() {
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
    public DataLakePathCreateOptions setLeaseDuration(int duration) {
        leaseDuration = duration;
        return this;
    }

    /**
     * @return the expiry options.
     */
    public PathExpiryMode getExpiryOptions() {
        return expiryOptions;
    }

    /**
     * Sets the expiry options.
     * @param options the new expiry options.
     * @return The updated options.
     */
    public DataLakePathCreateOptions setExpiryOptions(PathExpiryMode options) {
        expiryOptions = options;
        return this;
    }

}
