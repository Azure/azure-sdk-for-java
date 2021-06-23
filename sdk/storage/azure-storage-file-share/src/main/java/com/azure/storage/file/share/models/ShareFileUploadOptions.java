// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.models;

import com.azure.storage.common.ParallelTransferOptions;
import com.azure.storage.common.implementation.StorageImplUtils;
import reactor.core.publisher.Flux;

import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Extended options that may be passed when uploading a file range.
 */
public final class ShareFileUploadOptions {
    private final Flux<ByteBuffer> dataFlux;
    private final InputStream dataStream;
    private final Long length;
    private Long offset;
    private ParallelTransferOptions parallelTransferOptions;
    private ShareRequestConditions requestConditions;

    /**
     * Constructs a new {@code FileParallelUploadOptions}.
     *
     * @param dataFlux The data to write to the file. Unlike other upload methods, this method does not require that
     * the {@code Flux} be replayable. In other words, it does not have to support multiple subscribers and is not
     * expected to produce the same values across subscriptions.
     * data provided in the {@link InputStream}.
     */
    public ShareFileUploadOptions(Flux<ByteBuffer> dataFlux) {
        StorageImplUtils.assertNotNull("dataFlux", dataFlux);
        this.dataFlux = dataFlux;
        this.dataStream = null;
        this.length = null;
    }

    /**
     * Constructs a new {@code FileParallelUploadOptions}.
     *
     * Use {@link #ShareFileUploadOptions(InputStream)} instead to supply an InputStream without knowing the exact
     * length beforehand.
     *
     * @param dataStream The data to write to the file. The data must be markable. This is in order to support retries.
     * If the data is not markable, consider wrapping your data source in a {@link java.io.BufferedInputStream} to add
     * mark support.
     * @param length The exact length of the data. It is important that this value match precisely the length of the
     * data provided in the {@link InputStream}.
     * @deprecated length is no longer necessary; use {@link #ShareFileUploadOptions(InputStream)} instead.
     */
    @Deprecated
    public ShareFileUploadOptions(InputStream dataStream, long length) {
        StorageImplUtils.assertNotNull("dataStream", length);
        StorageImplUtils.assertInBounds("length", length, 0, Long.MAX_VALUE);
        this.dataStream = dataStream;
        this.length = length;
        this.dataFlux = null;
    }

    /**
     * Constructs a new {@code FileParallelUploadOptions}.
     *
     * @param dataStream The data to write to the file. The data must be markable. This is in order to support retries.
     * If the data is not markable, consider wrapping your data source in a {@link java.io.BufferedInputStream} to add
     * mark support.
     */
    public ShareFileUploadOptions(InputStream dataStream) {
        StorageImplUtils.assertNotNull("dataStream", dataStream);
        this.dataStream = dataStream;
        this.dataFlux = null;
        this.length = null;
    }

    /**
     * Gets the data source.
     *
     * @return The data to write to the file.
     */
    public Flux<ByteBuffer> getDataFlux() {
        return this.dataFlux;
    }

    /**
     * Gets the data source.
     *
     * @return The data to write to the file.
     */
    public InputStream getDataStream() {
        return this.dataStream;
    }

    /**
     * Gets the length of the data associated with an {@link InputStream} or {@link Flux}&lt;{@link ByteBuffer}&gt;.
     *
     * @return The exact length of the data. It is important that this value match precisely the length of the
     * data provided in the {@link InputStream} or {@link Flux}&lt;{@link ByteBuffer}&gt;.
     */
    public Long getLength() {
        return length;
    }

    /**
     * Gets the offset to start writing data at.
     *
     * @return {@link Long} position to write at.
     */
    public Long getOffset() {
        return offset;
    }

    /**
     * Sets the offset to start writing data at.
     *
     * @param offset {@link Long} position to write at.
     * @return The updated options.
     */
    public ShareFileUploadOptions setOffset(Long offset) {
        this.offset = offset;
        return this;
    }

    /**
     * Gets the {@link ParallelTransferOptions}.
     *
     * @return {@link ParallelTransferOptions}
     */
    public ParallelTransferOptions getParallelTransferOptions() {
        return parallelTransferOptions;
    }

    /**
     * Sets the {@link ParallelTransferOptions}.
     *
     * @param parallelTransferOptions {@link ParallelTransferOptions}
     * @return The updated options.
     */
    public ShareFileUploadOptions setParallelTransferOptions(ParallelTransferOptions parallelTransferOptions) {
        this.parallelTransferOptions = parallelTransferOptions;
        return this;
    }

    /**
     * Gets the {@link ShareRequestConditions}.
     *
     * @return {@link ShareRequestConditions}
     */
    public ShareRequestConditions getRequestConditions() {
        return requestConditions;
    }

    /**
     * Sets the {@link ShareRequestConditions}.
     *
     * @param requestConditions {@link ShareRequestConditions}
     * @return The updated options.
     */
    public ShareFileUploadOptions setRequestConditions(ShareRequestConditions requestConditions) {
        this.requestConditions = requestConditions;
        return this;
    }
}
