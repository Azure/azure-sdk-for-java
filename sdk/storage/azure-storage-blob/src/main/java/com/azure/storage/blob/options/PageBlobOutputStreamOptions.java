// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.options;

import com.azure.core.annotation.Fluent;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.PageRange;
import com.azure.storage.common.StorageChecksumAlgorithm;
import com.azure.storage.common.implementation.StorageImplUtils;

/**
 * Extended options that may be passed when opening an output stream to a page blob.
 */
@Fluent
public final class PageBlobOutputStreamOptions {
    private final PageRange pageRange;
    private BlobRequestConditions requestConditions;
    private StorageChecksumAlgorithm transferValidationChecksumAlgorithm;

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
     * Gets the algorithm to use for transfer content validation on the request. See {@link StorageChecksumAlgorithm}
     * for more details.
     *
     * @return The transfer validation checksum algorithm.
     */
    public StorageChecksumAlgorithm getTransferValidationChecksumAlgorithm() {
        return transferValidationChecksumAlgorithm;
    }

    /**
     * Sets the algorithm to use for transfer content validation on the request. See {@link StorageChecksumAlgorithm}
     * for more details.
     *
     * @param transferValidationChecksumAlgorithm The transfer validation checksum algorithm.
     * @return The updated options.
     */
    public PageBlobOutputStreamOptions
        setTransferValidationChecksumAlgorithm(StorageChecksumAlgorithm transferValidationChecksumAlgorithm) {
        this.transferValidationChecksumAlgorithm = transferValidationChecksumAlgorithm;
        return this;
    }
}
