// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.storage.blob.models.BlobAccessConditions;

import java.time.Duration;

/**
 * HTTPGetterInfo is a passed to the getter function of a reliable download to specify parameters needed for the GET
 * request.
 */
final class HTTPGetterInfo {
    private long offset = 0;

    private Long count = null;

    private String eTag = null;

    /**
     * The start offset that should be used when creating the HTTP GET request's Range header. Defaults to 0.
     */
    public long offset() {
        return offset;
    }

    /**
     * The start offset that should be used when creating the HTTP GET request's Range header. Defaults to 0.
     */
    public HTTPGetterInfo offset(long offset) {
        this.offset = offset;
        return this;
    }

    /**
     * The count of bytes that should be used to calculate the end offset when creating the HTTP GET request's Range
     * header. {@code} null is the default and indicates that the entire rest of the blob should be retrieved.
     */
    public Long count() {
        return count;
    }

    /**
     * The count of bytes that should be used to calculate the end offset when creating the HTTP GET request's Range
     * header. {@code} null is the default and indicates that the entire rest of the blob should be retrieved.
     */
    public HTTPGetterInfo count(Long count) {
        if (count != null) {
            Utility.assertInBounds("count", count, 0, Long.MAX_VALUE);
        }
        this.count = count;
        return this;
    }

    /**
     * The resource's etag that should be used when creating the HTTP GET request's If-Match header. Note that the
     * Etag is returned with any operation that modifies the resource and by a call to {@link
     * BlobClient#getProperties(BlobAccessConditions, Duration)}. Defaults to null.
     */
    public String eTag() {
        return eTag;
    }

    /**
     * The resource's etag that should be used when creating the HTTP GET request's If-Match header. Note that the
     * Etag is returned with any operation that modifies the resource and by a call to {@link
     * BlobClient#getProperties(BlobAccessConditions, Duration)}. Defaults to null.
     */
    public HTTPGetterInfo eTag(String eTag) {
        this.eTag = eTag;
        return this;
    }
}
