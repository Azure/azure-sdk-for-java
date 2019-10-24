// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import com.azure.core.annotation.Fluent;

import java.time.OffsetDateTime;

/**
 * This class contains values that restrict the successful completion of PageBlob operations to certain conditions.
 * It may be set to null if no access conditions are desired.
 * <p>
 * Please refer to the request header section
 * <a href=https://docs.microsoft.com/en-us/rest/api/storageservices/put-page>here</a> for more conceptual information.
 */
@Fluent
public final class PageBlobRequestConditions extends BlobRequestConditions {
    private Long ifSequenceNumberLessThanOrEqualTo;
    private Long ifSequenceNumberLessThan;
    private Long ifSequenceNumberEqualTo;

    /**
     * Optionally limit requests to resources that match the passed ETag.
     *
     * @param ifMatch ETag that resources must match.
     * @return The updated PageBlobRequestConditions object.
     */
    @Override
    public PageBlobRequestConditions setIfMatch(String ifMatch) {
        super.setIfMatch(ifMatch);
        return this;
    }

    /**
     * Optionally limit requests to resources that do not match the passed ETag.
     *
     * @param ifNoneMatch ETag that resources must not match.
     * @return The updated PageBlobRequestConditions object.
     */
    @Override
    public PageBlobRequestConditions setIfNoneMatch(String ifNoneMatch) {
        super.setIfNoneMatch(ifNoneMatch);
        return this;
    }

    /**
     * Optionally limit requests to resources that have only been modified since the passed
     * {@link OffsetDateTime datetime}.
     *
     * @param ifModifiedSince The datetime that resources must have been modified since.
     * @return The updated PageBlobRequestConditions object.
     */
    @Override
    public PageBlobRequestConditions setIfModifiedSince(OffsetDateTime ifModifiedSince) {
        super.setIfModifiedSince(ifModifiedSince);
        return this;
    }

    /**
     * Optionally limit requests to resources that have remained unmodified since the passed
     * {@link OffsetDateTime datetime}.
     *
     * @param ifUnmodifiedSince The datetime that resources must have remained unmodified since.
     * @return The updated PageBlobRequestConditions object.
     */
    @Override
    public PageBlobRequestConditions setIfUnmodifiedSince(OffsetDateTime ifUnmodifiedSince) {
        super.setIfUnmodifiedSince(ifUnmodifiedSince);
        return this;
    }

    /**
     * Optionally limits requests to blobs and containers that match the lease ID.
     *
     * @param leaseId Lease ID that blobs and containers must match.
     * @return The updated PageBlobRequestConditions object.
     */
    @Override
    public PageBlobRequestConditions setLeaseId(String leaseId) {
        super.setLeaseId(leaseId);
        return this;
    }

    /**
     * Gets the value that the page blob's sequence number must be less than or equal to.
     *
     * @return The value the sequence number must be less than or equal to.
     */
    public Long getIfSequenceNumberLessThanOrEqualTo() {
        return ifSequenceNumberLessThanOrEqualTo;
    }

    /**
     * Sets the value that the page blob's sequence number must be less than or equal to.
     *
     * @param ifSequenceNumberLessThanOrEqualTo The value the sequence number must be less than or equal to.
     * @return The updated PageBlobRequestConditions object.
     */
    public PageBlobRequestConditions setIfSequenceNumberLessThanOrEqualTo(Long ifSequenceNumberLessThanOrEqualTo) {
        this.ifSequenceNumberLessThanOrEqualTo = ifSequenceNumberLessThanOrEqualTo;
        return this;
    }

    /**
     * Gets the value that the page blob's sequence number must be less than.
     *
     * @return The value the sequence number must be less than.
     */
    public Long getIfSequenceNumberLessThan() {
        return ifSequenceNumberLessThan;
    }

    /**
     * Sets the value that the page blob's sequence number must be less than.
     *
     * @param ifSequenceNumberLessThan The value the sequence number must be less than.
     * @return The updated PageBlobRequestConditions object.
     */
    public PageBlobRequestConditions setIfSequenceNumberLessThan(Long ifSequenceNumberLessThan) {
        this.ifSequenceNumberLessThan = ifSequenceNumberLessThan;
        return this;
    }

    /**
     * Gets the value that the page blob's sequence number must be equal to.
     *
     * @return The value the sequence number must be equal to.
     */
    public Long getIfSequenceNumberEqualTo() {
        return ifSequenceNumberEqualTo;
    }

    /**
     * Sets the value that the page blob's sequence number must be less equal to.
     *
     * @param ifSequenceNumberEqualTo The value the sequence number must be equal to.
     * @return The updated PageBlobRequestConditions object.
     */
    public PageBlobRequestConditions setIfSequenceNumberEqualTo(Long ifSequenceNumberEqualTo) {
        this.ifSequenceNumberEqualTo = ifSequenceNumberEqualTo;
        return this;
    }
}
