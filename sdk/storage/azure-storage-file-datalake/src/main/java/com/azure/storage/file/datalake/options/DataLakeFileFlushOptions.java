// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.options;

import com.azure.core.annotation.Fluent;
import com.azure.storage.file.datalake.DataLakeFileAsyncClient;
import com.azure.storage.file.datalake.DataLakeFileClient;
import com.azure.storage.file.datalake.models.LeaseAction;
import com.azure.storage.file.datalake.models.DataLakeRequestConditions;
import com.azure.storage.file.datalake.models.PathHttpHeaders;


/**
 * Optional parameters for appending data to a file when calling flush() on {@link DataLakeFileClient} and
 * {@link DataLakeFileAsyncClient}
 */
@Fluent
public class DataLakeFileFlushOptions {

    private Boolean retainUncommittedData;
    private Boolean close;
    private PathHttpHeaders pathHttpHeaders;
    private DataLakeRequestConditions requestConditions;
    private LeaseAction leaseAction;
    private Integer leaseDuration;
    private String proposedLeaseId;

    /**
     * If "true", uncommitted data is retained after the flush operation completes; otherwise, the uncommitted data is
     * deleted after the flush operation. The default is false. Data at offsets less than the specified position are
     * written to the file when flush succeeds, but this optional parameter allows data after the flush position to be
     * retained for a future flush operation.
     *
     * @return whether to retain uncommitted data.
     */
    public Boolean isUncommittedDataRetained() {
        return retainUncommittedData;
    }

    /**
     * Sets whether uncommitted data should be retained. If "true", uncommitted data is retained after the flush
     * operation completes; otherwise, the uncommitted data is deleted after the flush operation. The default is false.
     * Data at offsets less than the specified position are written to the file when flush succeeds, but this optional
     * parameter allows data after the flush position to be retained for a future flush operation.
     *
     * @param retainUncommittedData boolean flag to indicate whether uncommitted data should be retained.
     * @return the updated DataLakeFileFlushOptions object.
     */
    public DataLakeFileFlushOptions setUncommittedDataRetained(Boolean retainUncommittedData) {
        this.retainUncommittedData = retainUncommittedData;
        return this;
    }

    /**
     * Azure Storage Events allow applications to receive notifications when files change. When Azure Storage Events are
     * enabled, a file changed event is raised. This event has a property indicating whether this is the final change to
     * distinguish the difference between an intermediate flush to a file stream and the final close of a file stream.
     * The close query parameter is valid only when the action is "flush" and change notifications are enabled. If the
     * value of close is "true" and the flush operation completes successfully, the service raises a file change
     * notification with a property indicating that this is the final update (the file stream has been closed).
     * If "false" a change notification is raised indicating the file has changed. The default is false.
     * This query parameter is set to true by the Hadoop ABFS driver to indicate that the file stream has been closed.
     *
     * @return whether the file stream has been closed.
     */
    public Boolean isClose() {
        return close;
    }

    /**
     * Sets whether file stream has been closed.
     * Azure Storage Events allow applications to receive notifications when files change. When Azure Storage Events are
     * enabled, a file changed event is raised. This event has a property indicating whether this is the final change to
     * distinguish the difference between an intermediate flush to a file stream and the final close of a file stream.
     * The close query parameter is valid only when the action is "flush" and change notifications are enabled. If the
     * value of close is "true" and the flush operation completes successfully, the service raises a file change
     * notification with a property indicating that this is the final update (the file stream has been closed).
     * If "false" a change notification is raised indicating the file has changed. The default is false.
     * This query parameter is set to true by the Hadoop ABFS driver to indicate that the file stream has been closed.
     *
     * @param close boolean flag to indicate whether file stream has been closed.
     * @return the updated DataLakeFileFlushOptions object.
     */
    public DataLakeFileFlushOptions setClose(Boolean close) {
        this.close = close;
        return this;
    }

    /**
     * Optional standard HTTP header properties for the file.
     *
     * @return the {@link PathHttpHeaders} for this file.
     */
    public PathHttpHeaders getPathHttpHeaders() {
        return pathHttpHeaders;
    }

    /**
     * Optional standard HTTP header properties that can be set for the file.
     *
     * @param pathHttpHeaders {@link PathHttpHeaders} to be set for this file.
     * @return the updated DataLakeFileFlushOptions object.
     */
    public DataLakeFileFlushOptions setPathHttpHeaders(PathHttpHeaders pathHttpHeaders) {
        this.pathHttpHeaders = pathHttpHeaders;
        return this;
    }

    /**
     * Optional {@link DataLakeRequestConditions} conditions that are set on the flush of this file.
     *
     * @return {@link DataLakeRequestConditions} for this file.
     */
    public DataLakeRequestConditions getRequestConditions() {
        return requestConditions;
    }

    /**
     * Optional {@link DataLakeRequestConditions} conditions to add on the flush of this file.
     *
     * @param requestConditions {@link DataLakeRequestConditions} to set on this file.
     * @return the updated DataLakeFileFlushOptions object.
     */
    public DataLakeFileFlushOptions setRequestConditions(DataLakeRequestConditions requestConditions) {
        this.requestConditions = requestConditions;
        return this;
    }

    /**
     * Get lease action set on file.
     * {@link LeaseAction#ACQUIRE} will attempt to acquire a new lease on the file,
     * with {@link DataLakeFileFlushOptions#proposedLeaseId} as the lease ID.
     * {@link LeaseAction#ACQUIRE_RELEASE} will attempt to acquire a new lease on the file,
     * with {@link DataLakeFileFlushOptions#proposedLeaseId} as the lease ID. The lease will be released once the
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
     * with {@link DataLakeFileFlushOptions#proposedLeaseId} as the lease ID.
     * {@link LeaseAction#ACQUIRE_RELEASE} will attempt to acquire a new lease on the file,
     * with {@link DataLakeFileFlushOptions#proposedLeaseId} as the lease ID. The lease will be released once the
     * Append operation is complete.
     * {@link LeaseAction#AUTO_RENEW} will attempt to renew the lease specified by {@link DataLakeRequestConditions#getLeaseId()}.
     * {@link LeaseAction#RELEASE} will attempt to release the least specified by {@link DataLakeRequestConditions#getLeaseId()}.
     *
     * @param leaseAction the {@link LeaseAction} to set on the file.
     * @return the updated DataLakeFileFlushOptions object.
     */
    public DataLakeFileFlushOptions setLeaseAction(LeaseAction leaseAction) {
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
     * Sets the lease duration.
     * Optional. Specifies the duration of the lease, in seconds, or specify -1 for a lease that never expires.
     * A non-infinite lease can be between 15 and 60 seconds.
     *
     * @param leaseDurationInSeconds the new lease duration.
     * @return the updated DataLakeFileFlushOptions object.
     */
    public DataLakeFileFlushOptions setLeaseDuration(Integer leaseDurationInSeconds) {
        this.leaseDuration = leaseDurationInSeconds;
        return this;
    }

    /**
     * Gets proposed lease id. Valid with {@link LeaseAction#ACQUIRE} and {@link LeaseAction#ACQUIRE_RELEASE}.
     *
     * @return the proposed lease id.
     */
    public String getProposedLeaseId() {
        return proposedLeaseId;
    }

    /**
     * Sets the proposed lease id. Valid with {@link LeaseAction#ACQUIRE} and {@link LeaseAction#ACQUIRE_RELEASE}.
     *
     * @param proposedLeaseId the proposed lease id to set.
     * @return the updated DataLakeFileFlushOptions object.
     */
    public DataLakeFileFlushOptions setProposedLeaseId(String proposedLeaseId) {
        this.proposedLeaseId = proposedLeaseId;
        return this;
    }
}
