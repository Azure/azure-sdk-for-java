// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.options;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.CoreUtils;
import com.azure.storage.file.datalake.models.LeaseAction;
import com.azure.storage.file.datalake.models.DataLakeRequestConditions;

/**
 * Optional parameters for appending data to a file.
 */
@Fluent
public class DataLakeFileAppendOptions {

    private String leaseId;
    private byte[] contentMd5;
    private Boolean flush;
    private LeaseAction leaseAction;
    private Integer leaseDuration;
    private String proposedLeaseId;

    /**
     * Gets the lease ID to access the file.
     *
     * @return lease ID to access this file.
     */
    public String getLeaseId() {
        return leaseId;
    }

    /**
     * Sets the lease ID.
     *
     * @param leaseId The lease ID.
     * @return the updated DataLakeFileAppendOptions object.
     */
    public DataLakeFileAppendOptions setLeaseId(String leaseId) {
        this.leaseId = leaseId;
        return this;
    }

    /**
     * When this header is specified, the storage service compares the hash of the content that has arrived with this
     * header value. If the two hashes do not match, the operation will fail with error code 400 (Bad Request). Note
     * that this MD5 hash is not stored with the file. This header is associated with the request content, and not with
     * the stored content of the file itself.
     *
     * @return MD5 hash of the content of the data.
     */
    public byte[] getContentMd5() {
        return CoreUtils.clone(this.contentMd5);
    }

    /**
     * This hash is used to verify the integrity of the request content during transport. When this header is specified,
     * the storage service compares the hash of the content that has arrived with this header value. If the two hashes
     * do not match, the operation will fail with error code 400 (Bad Request). Note that this MD5 hash is not stored
     * with the file. This header is associated with the request content, and not with the stored content of the file itself.
     *
     * @param contentMd5 contentMd5 An MD5 hash of the content of the data. If specified, the service will calculate
     * the MD5 of the received data and fail the request if it does not match the provided MD5.
     * @return the updated DataLakeFileAppendOptions object.
     */
    public DataLakeFileAppendOptions setContentHash(byte[] contentMd5) {
        this.contentMd5 = CoreUtils.clone(contentMd5);
        return this;
    }

    /**
     * Returns whether file will be flushed after the append.
     *
     * @return the boolean flag for flush.
     */
    public Boolean isFlush() {
        return flush;
    }

    /**
     * If true, the file will be flushed after the append.
     *
     * @param flush boolean flag to indicate whether file should be flushed.
     * @return the updated DataLakeFileAppendOptions object.
     */
    public DataLakeFileAppendOptions setFlush(Boolean flush) {
        this.flush = flush;
        return this;
    }

    /**
     * Get lease action set on file.
     * {@link LeaseAction#ACQUIRE} will attempt to acquire a new lease on the file,
     * with {@link DataLakeFileAppendOptions#proposedLeaseId} as the lease ID.
     * {@link LeaseAction#ACQUIRE_RELEASE} will attempt to aquire a new lease on the file,
     * with {@link DataLakeFileAppendOptions#proposedLeaseId} as the lease ID. The lease will be released once the
     * Append operation is complete.
     * {@link LeaseAction#AUTO_RENEW} will attempt to renew the lease specified by {@link DataLakeRequestConditions#getLeaseId()}.
     * {@link LeaseAction#RELEASE} will attempt to release the least specified by {@link DataLakeRequestConditions#getLeaseId()}.
     *
     * @return The {@link LeaseAction} set on the file.
     */
    public LeaseAction getLeaseAction() {
        return leaseAction;
    }

    /**
     * Get lease action set on file.
     * {@link LeaseAction#ACQUIRE} will attempt to acquire a new lease on the file,
     * with {@link DataLakeFileAppendOptions#proposedLeaseId} as the lease ID.
     * {@link LeaseAction#ACQUIRE_RELEASE} will attempt to aquire a new lease on the file,
     * with {@link DataLakeFileAppendOptions#proposedLeaseId} as the lease ID. The lease will be released once the
     * Append operation is complete.
     * {@link LeaseAction#AUTO_RENEW} will attempt to renew the lease specified by {@link DataLakeRequestConditions#getLeaseId()}.
     * {@link LeaseAction#RELEASE} will attempt to release the least specified by {@link DataLakeRequestConditions#getLeaseId()}.
     *
     * @param leaseAction the {@link LeaseAction} to set on the file.
     * @return the updated DataLakeFileAppendOptions object.
     */
    public DataLakeFileAppendOptions setLeaseAction(LeaseAction leaseAction) {
        this.leaseAction = leaseAction;
        return this;
    }

    /**
     * @return the lease duration in seconds.
     */
    public Integer getLeaseDuration() {
        return leaseDuration;
    }

    /**
     * Optional. Specifies the duration of the lease, in seconds, or specify -1 for a lease that never expires.
     * A non-infinite lease can be between 15 and 60 seconds.
     *
     * Sets the lease duration.
     * @param leaseDurationInSeconds the new lease duration.
     * @return the updated DataLakeFileAppendOptions object.
     */
    public DataLakeFileAppendOptions setLeaseDuration(Integer leaseDurationInSeconds) {
        this.leaseDuration = leaseDurationInSeconds;
        return this;
    }

    /**
     * Gets proposed lease id. Valid with {@link LeaseAction#ACQUIRE} and {@link LeaseAction#ACQUIRE_RELEASE}.
     * @return the proposed lease id.
     */
    public String getProposedLeaseId() {
        return proposedLeaseId;
    }

    /**
     * Sets the proposed lease id. Valid with {@link LeaseAction#ACQUIRE} and {@link LeaseAction#ACQUIRE_RELEASE}.
     * @param proposedLeaseId the proposed lease id to set.
     * @return the updated DataLakeFileAppendOptions object.
     */
    public DataLakeFileAppendOptions setProposedLeaseId(String proposedLeaseId) {
        this.proposedLeaseId = proposedLeaseId;
        return this;
    }
}
