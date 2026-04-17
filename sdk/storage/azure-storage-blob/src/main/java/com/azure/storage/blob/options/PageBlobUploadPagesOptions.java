// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.options;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.CoreUtils;
import com.azure.storage.blob.models.PageBlobRequestConditions;
import com.azure.storage.blob.models.PageRange;
import com.azure.storage.common.ContentValidationAlgorithm;
import com.azure.storage.common.implementation.StorageImplUtils;

import reactor.core.publisher.Flux;

import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Extended options that may be passed when uploading pages to a page blob.
 */
@Fluent
public final class PageBlobUploadPagesOptions {
    private final PageRange pageRange;
    private final Flux<ByteBuffer> dataFlux;
    private final InputStream dataStream;
    private byte[] contentMd5;
    private PageBlobRequestConditions requestConditions;
    private ContentValidationAlgorithm contentValidationAlgorithm;

    /**
     * Creates a new instance of {@link PageBlobUploadPagesOptions}.
     *
     * @param pageRange The {@link PageRange} for the upload. Pages must be aligned with 512-byte boundaries.
     * @param body The data to upload. Must be replayable if retries are enabled.
     * @throws NullPointerException If {@code pageRange} or {@code body} is null.
     */
    public PageBlobUploadPagesOptions(PageRange pageRange, Flux<ByteBuffer> body) {
        StorageImplUtils.assertNotNull("pageRange", pageRange);
        StorageImplUtils.assertNotNull("body", body);
        this.pageRange = pageRange;
        this.dataFlux = body;
        this.dataStream = null;
    }

    /**
     * Creates a new instance of {@link PageBlobUploadPagesOptions}.
     *
     * @param pageRange The {@link PageRange} for the upload. Pages must be aligned with 512-byte boundaries.
     * @param body The data to upload. Must be markable for retries.
     * @throws NullPointerException If {@code pageRange} or {@code body} is null.
     */
    public PageBlobUploadPagesOptions(PageRange pageRange, InputStream body) {
        StorageImplUtils.assertNotNull("pageRange", pageRange);
        StorageImplUtils.assertNotNull("body", body);
        this.pageRange = pageRange;
        this.dataFlux = null;
        this.dataStream = body;
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
     * Gets the body as a Flux. Null if constructed with InputStream.
     *
     * @return The body flux, or null.
     */
    public Flux<ByteBuffer> getDataFlux() {
        return dataFlux;
    }

    /**
     * Gets the body as an InputStream. Null if constructed with Flux.
     *
     * @return The body stream, or null.
     */
    public InputStream getDataStream() {
        return dataStream;
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
