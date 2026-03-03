// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.options;

import com.azure.core.annotation.Fluent;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.PageRange;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.common.implementation.contentvalidation.StorageChecksumAlgorithm;

/**
 * Extended options that may be passed when opening an output stream to a page blob.
 */
@Fluent
public final class PageBlobOutputStreamOptions {
    private final PageRange pageRange;
    private BlobRequestConditions requestConditions;
    private StorageChecksumAlgorithm requestChecksumAlgorithm;

    /**
     * Creates a new instance of {@link PageBlobOutputStreamOptions}.
     *
     * @param pageRange The {@link PageRange} for the write. Pages must be aligned with 512-byte boundaries.
     * @throws NullPointerException If {@code pageRange} is null.
     */
    public PageBlobOutputStreamOptions(PageRange pageRange) {
        StorageImplUtils.assertNotNull("pageRange", pageRange);
        this.pageRange = pageRange;
    }

    /**
     * Gets the page range.
     *
     * @return The page range.
     */
    public PageRange getPageRange() {
        return pageRange;
    }

    /**
     * Gets the {@link BlobRequestConditions}.
     *
     * @return The request conditions.
     */
    public BlobRequestConditions getRequestConditions() {
        return requestConditions;
    }

    /**
     * Sets the {@link BlobRequestConditions}.
     *
     * @param requestConditions The request conditions.
     * @return The updated options.
     */
    public PageBlobOutputStreamOptions setRequestConditions(BlobRequestConditions requestConditions) {
        this.requestConditions = requestConditions;
        return this;
    }

    /**
     * Gets the algorithm to use for request content validation. Default is {@link StorageChecksumAlgorithm#NONE}.
     *
     * @return The request checksum algorithm.
     */
    public StorageChecksumAlgorithm getRequestChecksumAlgorithm() {
        return requestChecksumAlgorithm;
    }

    /**
     * Sets the algorithm to use for request content validation. When set to {@link StorageChecksumAlgorithm#AUTO},
     * {@link StorageChecksumAlgorithm#CRC64}, or {@link StorageChecksumAlgorithm#MD5}, the SDK will compute and send
     * checksums for upload validation. Default is {@link StorageChecksumAlgorithm#NONE}.
     *
     * @param requestChecksumAlgorithm The request checksum algorithm.
     * @return The updated options.
     */
    public PageBlobOutputStreamOptions setRequestChecksumAlgorithm(StorageChecksumAlgorithm requestChecksumAlgorithm) {
        this.requestChecksumAlgorithm = requestChecksumAlgorithm;
        return this;
    }
}
