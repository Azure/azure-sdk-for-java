// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.options;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.BinaryData;
import com.azure.core.util.CoreUtils;
import com.azure.storage.blob.models.PageBlobRequestConditions;
import com.azure.storage.blob.models.PageRange;
import com.azure.storage.common.ContentValidationAlgorithm;
import com.azure.storage.common.implementation.StorageImplUtils;

/**
 * Extended options that may be passed when uploading pages to a page blob.
 */
@Fluent
public final class PageBlobUploadPagesOptions {
    private final PageRange pageRange;
    private final BinaryData body;
    private byte[] contentMd5;
    private PageBlobRequestConditions requestConditions;
    private ContentValidationAlgorithm contentValidationAlgorithm;

    /**
     * Creates a new instance of {@link PageBlobUploadPagesOptions}.
     *
     * @param pageRange A {@link PageRange} object. Given that pages must be aligned with 512-byte boundaries, the start
     * offset must be a modulus of 512 and the end offset must be a modulus of 512 - 1. Examples of valid byte ranges
     * are 0-511, 512-1023, etc.
     * @param body The data to write to the page. Note that this {@code BinaryData} must have defined length
     * and must be replayable if retries are enabled (the default), see {@link BinaryData#isReplayable()}.
     * @throws NullPointerException If {@code pageRange} or {@code body} is null, or if {@code body} does not have a
     * defined length.
     */
    public PageBlobUploadPagesOptions(PageRange pageRange, BinaryData body) {
        StorageImplUtils.assertNotNull("pageRange must not be null", pageRange);
        StorageImplUtils.assertNotNull("body must not be null", body);
        StorageImplUtils.assertNotNull("body must have defined length", body.getLength());
        this.pageRange = pageRange;
        this.body = body;
    }

    /**
     * Gets the page range for the request.
     *
     * @return The page range for the request.
     */
    public PageRange getPageRange() {
        return this.pageRange;
    }

    /**
     * Gets the data to write to the page.
     *
     * @return The data to write to the page.
     */
    public BinaryData getBody() {
        return this.body;
    }

    /**
     * Gets the MD5 hash of the page content.
     *
     * @return An MD5 hash of the content, or null.
     */
    public byte[] getContentMd5() {
        return CoreUtils.clone(contentMd5);
    }

    /**
     * Sets the MD5 hash of the page content for transactional verification.
     *
     * @param contentMd5 An MD5 hash of the page content.
     * @return The updated options.
     */
    public PageBlobUploadPagesOptions setContentMd5(byte[] contentMd5) {
        this.contentMd5 = CoreUtils.clone(contentMd5);
        return this;
    }

    /**
     * Gets the {@link PageBlobRequestConditions}.
     *
     * @return The request conditions.
     */
    public PageBlobRequestConditions getRequestConditions() {
        return requestConditions;
    }

    /**
     * Sets the {@link PageBlobRequestConditions}.
     *
     * @param requestConditions The request conditions.
     * @return The updated options.
     */
    public PageBlobUploadPagesOptions setRequestConditions(PageBlobRequestConditions requestConditions) {
        this.requestConditions = requestConditions;
        return this;
    }

    /**
     * Gets the algorithm to use for transfer content validation on the request. See {@link ContentValidationAlgorithm}
     * for more details.
     *
     * @return The transfer validation checksum algorithm.
     */
    public ContentValidationAlgorithm getContentValidationAlgorithm() {
        return contentValidationAlgorithm;
    }

    /**
     * Sets the algorithm to use for transfer content validation on the request. See {@link ContentValidationAlgorithm}
     * for more details.
     *
     * @param contentValidationAlgorithm The transfer validation checksum algorithm.
     * @return The updated options.
     */
    public PageBlobUploadPagesOptions
        setContentValidationAlgorithm(ContentValidationAlgorithm contentValidationAlgorithm) {
        this.contentValidationAlgorithm = contentValidationAlgorithm;
        return this;
    }
}
