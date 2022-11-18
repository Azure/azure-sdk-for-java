// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.models;

import com.azure.core.util.BinaryData;
import com.azure.storage.common.implementation.StorageImplUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * Extended options that may be passed when uploading a file range.
 */
public final class ShareFileUploadRangeOptions {
    private final Mono<BinaryData> dataMono;
    private Long offset;
    private ShareRequestConditions requestConditions;
    private FileLastWrittenMode lastWrittenMode;

    /**
     * Constructs a new {@code FileParallelUploadOptions}.
     *
     * @param dataFlux The data to write to the file. Unlike other upload methods, this method does not require that
     * the {@code Flux} be replayable. In other words, it does not have to support multiple subscribers and is not
     * expected to produce the same values across subscriptions.
     * @param length The exact length of the data. It is important that this value match precisely the length of the
     * data provided in the {@link InputStream}.
     */
    public ShareFileUploadRangeOptions(Flux<ByteBuffer> dataFlux, long length) {
        StorageImplUtils.assertNotNull("dataFlux", dataFlux);
        StorageImplUtils.assertInBounds("length", length, 0, Long.MAX_VALUE);
        this.dataMono = BinaryData.fromFlux(dataFlux, length);
    }

    /**
     * Constructs a new {@code FileParallelUploadOptions}.
     *
     * @param dataStream The data to write to the file. The data must be markable. This is in order to support retries.
     * If the data is not markable, consider wrapping your data source in a {@link java.io.BufferedInputStream} to add
     * mark support.
     * @param length The exact length of the data. It is important that this value match precisely the length of the
     * data provided in the {@link InputStream}. Value must be greater than or equal to 1.
     */
    public ShareFileUploadRangeOptions(InputStream dataStream, long length) {
        StorageImplUtils.assertNotNull("dataStream", length);
        StorageImplUtils.assertInBounds("length", length, 1, Long.MAX_VALUE);
        this.dataMono = Mono.just(BinaryData.fromStream(dataStream, length));
    }

    /**
     * Constructs a new {@code FileParallelUploadOptions}.
     *
     * @param data BinaryData.
     * @throws IllegalArgumentException if the BinaryData has no length.
     */
    public ShareFileUploadRangeOptions(BinaryData data) {
        StorageImplUtils.assertNotNull("data", data);
        if (data.getLength() == null) {
            throw new IllegalArgumentException("ShareFileUploadRangeOptions requires a length, but the given BinaryData had none.");
        }
        this.dataMono = Mono.just(data);
    }

    /**
     * Gets Mono of binary data.
     * @return mono of data
     */
    public Mono<BinaryData> getDataMono() {
        return this.dataMono;
    }


    /**
     * Gets the data source.
     *
     * @return The data to write to the file.
     */
    public Flux<ByteBuffer> getDataFlux() {
        return this.dataMono.flatMapMany(BinaryData::toFluxByteBuffer);
    }

    /**
     * Gets the data source.
     *
     * @return The data to write to the file.
     */
    public InputStream getDataStream() {
        return Objects.requireNonNull(this.dataMono.block()).toStream();
    }

    /**
     * Gets the length of the data associated with an {@link InputStream} or {@link Flux}&lt;{@link ByteBuffer}&gt;.
     *
     * @return The exact length of the data. It is important that this value match precisely the length of the
     * data provided in the {@link InputStream} or {@link Flux}&lt;{@link ByteBuffer}&gt;.
     */
    public long getLength() {
        return Objects.requireNonNull(dataMono.block()).getLength();
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
    public ShareFileUploadRangeOptions setOffset(Long offset) {
        this.offset = offset;
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
    public ShareFileUploadRangeOptions setRequestConditions(ShareRequestConditions requestConditions) {
        this.requestConditions = requestConditions;
        return this;
    }

    /**
     * Gets the {@link FileLastWrittenMode}.
     *
     * @return The {@link FileLastWrittenMode}.
     */
    public FileLastWrittenMode getLastWrittenMode() {
        return this.lastWrittenMode;
    }

    /**
     * Sets the {@link FileLastWrittenMode}.
     *
     * @param lastWrittenMode {@link FileLastWrittenMode}
     * @return The updated options.
     */
    public ShareFileUploadRangeOptions setLastWrittenMode(FileLastWrittenMode lastWrittenMode) {
        this.lastWrittenMode = lastWrittenMode;
        return this;
    }
}
