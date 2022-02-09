// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import com.azure.core.annotation.Fluent;

import java.time.OffsetDateTime;

/**
 * This class contains values that restrict the successful completion of AppendBlock operations to certain conditions.
 * Any field may be set to null if no access conditions are desired.
 * <p>
 * Please refer to the request header section
 * <a href=https://docs.microsoft.com/rest/api/storageservices/append-block>here</a> for more conceptual
 * information.
 */
@Fluent
public final class AppendBlobRequestConditions extends BlobRequestConditions {
    private Long maxSize;
    private Long appendPosition;

    /**
     * Optionally limit requests to resources that match the passed ETag.
     *
     * @param ifMatch ETag that resources must match.
     * @return The updated AppendBlobRequestConditions object.
     */
    @Override
    public AppendBlobRequestConditions setIfMatch(String ifMatch) {
        super.setIfMatch(ifMatch);
        return this;
    }

    /**
     * Optionally limit requests to resources that do not match the passed ETag.
     *
     * @param ifNoneMatch ETag that resources must not match.
     * @return The updated AppendBlobRequestConditions object.
     */
    @Override
    public AppendBlobRequestConditions setIfNoneMatch(String ifNoneMatch) {
        super.setIfNoneMatch(ifNoneMatch);
        return this;
    }

    /**
     * Optionally limit requests to resources that have only been modified since the passed
     * {@link OffsetDateTime datetime}.
     *
     * @param ifModifiedSince The datetime that resources must have been modified since.
     * @return The updated AppendBlobRequestConditions object.
     */
    @Override
    public AppendBlobRequestConditions setIfModifiedSince(OffsetDateTime ifModifiedSince) {
        super.setIfModifiedSince(ifModifiedSince);
        return this;
    }

    /**
     * Optionally limit requests to resources that have remained unmodified since the passed
     * {@link OffsetDateTime datetime}.
     *
     * @param ifUnmodifiedSince The datetime that resources must have remained unmodified since.
     * @return The updated AppendBlobRequestConditions object.
     */
    @Override
    public AppendBlobRequestConditions setIfUnmodifiedSince(OffsetDateTime ifUnmodifiedSince) {
        super.setIfUnmodifiedSince(ifUnmodifiedSince);
        return this;
    }

    /**
     * Optionally limits requests to blobs and containers that match the lease ID.
     *
     * @param leaseId Lease ID that blobs and containers must match.
     * @return The updated AppendBlobRequestConditions object.
     */
    @Override
    public AppendBlobRequestConditions setLeaseId(String leaseId) {
        super.setLeaseId(leaseId);
        return this;
    }

    /**
     * Optionally applies the SQL statement to the tags of the blob.
     *
     * @param tagsConditions The SQL statement that apply to the tags of the blob.
     * @return The updated BlobRequestConditions object.
     */
    @Override
    public AppendBlobRequestConditions setTagsConditions(String tagsConditions) {
        super.setTagsConditions(tagsConditions);
        return this;
    }

    /**
     * Gets the max length in bytes allowed for the append blob.
     *
     * <p>If the operation would cause the append blob to grow larger than the limit the request will fail.</p>
     *
     * @return The max length in bytes allowed for the append blob.
     */
    public Long getMaxSize() {
        return maxSize;
    }

    /**
     * Sets the max length in bytes allowed for the append blob.
     *
     * <p>If the operation would cause the append blob to grow larger than the limit the request will fail.</p>
     *
     * @param maxSize Append blob size byte limit.
     * @return The updated AppendBlobRequestConditions object.
     */
    public AppendBlobRequestConditions setMaxSize(Long maxSize) {
        this.maxSize = maxSize;
        return this;
    }

    /**
     * Gets the byte offset that the append position of the append blob must match.
     *
     * @return The byte offset that must match the append position.
     */
    public Long getAppendPosition() {
        return appendPosition;
    }

    /**
     * Sets the byte offset that the append position of the append blob must match.
     *
     * @param appendPosition Byte offset to compare to the append position.
     * @return The updated AppendBlobRequestConditions object.
     */
    public AppendBlobRequestConditions setAppendPosition(Long appendPosition) {
        this.appendPosition = appendPosition;
        return this;
    }
}
