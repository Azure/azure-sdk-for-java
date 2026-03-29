// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.options;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.CoreUtils;
import com.azure.storage.blob.models.AppendBlobRequestConditions;
import com.azure.storage.common.StorageChecksumAlgorithm;
import com.azure.storage.common.implementation.StorageImplUtils;

import reactor.core.publisher.Flux;

import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Extended options that may be passed when appending a block to an append blob.
 */
@Fluent
public final class AppendBlobAppendBlockOptions {
    private final InputStream bodyStream;
    private final Flux<ByteBuffer> bodyFlux;
    private final long length;
    private byte[] contentMd5;
    private AppendBlobRequestConditions requestConditions;
    private StorageChecksumAlgorithm requestChecksumAlgorithm;

    /**
     * Creates a new instance of {@link AppendBlobAppendBlockOptions} for use with the sync client.
     *
     * @param data The data to write to the blob. Must be markable for retries.
     * @param length The exact length of the data.
     * @throws NullPointerException If {@code data} is null.
     * @throws IllegalArgumentException If {@code length} is negative.
     */
    public AppendBlobAppendBlockOptions(InputStream data, long length) {
        StorageImplUtils.assertNotNull("data", data);
        if (length < 0) {
            throw new IllegalArgumentException("'length' must be >= 0");
        }
        this.bodyStream = data;
        this.bodyFlux = null;
        this.length = length;
    }

    /**
     * Creates a new instance of {@link AppendBlobAppendBlockOptions} for use with the async client.
     *
     * @param data The data to write to the blob. Must be replayable if retries are enabled.
     * @param length The exact length of the data.
     * @throws NullPointerException If {@code data} is null.
     * @throws IllegalArgumentException If {@code length} is negative.
     */
    public AppendBlobAppendBlockOptions(Flux<ByteBuffer> data, long length) {
        StorageImplUtils.assertNotNull("data", data);
        if (length < 0) {
            throw new IllegalArgumentException("'length' must be >= 0");
        }
        this.bodyStream = null;
        this.bodyFlux = data;
        this.length = length;
    }

    /**
     * Gets the body as an InputStream. Null if constructed with {@link Flux}.
     *
     * @return The body stream, or null.
     */
    public InputStream getBodyStream() {
        return bodyStream;
    }

    /**
     * Gets the body as a Flux. Null if constructed with {@link InputStream}.
     *
     * @return The body flux, or null.
     */
    public Flux<ByteBuffer> getBodyFlux() {
        return bodyFlux;
    }

    /**
     * Gets the exact length of the block data.
     *
     * @return The length in bytes.
     */
    public long getLength() {
        return length;
    }

    /**
     * Gets the MD5 hash of the block content.
     *
     * @return An MD5 hash of the content, or null.
     */
    public byte[] getContentMd5() {
        return CoreUtils.clone(contentMd5);
    }

    /**
     * Sets the MD5 hash of the block content for transactional verification.
     *
     * @param contentMd5 An MD5 hash of the block content.
     * @return The updated options.
     */
    public AppendBlobAppendBlockOptions setContentMd5(byte[] contentMd5) {
        this.contentMd5 = CoreUtils.clone(contentMd5);
        return this;
    }

    /**
     * Gets the {@link AppendBlobRequestConditions}.
     *
     * @return The request conditions.
     */
    public AppendBlobRequestConditions getRequestConditions() {
        return requestConditions;
    }

    /**
     * Sets the {@link AppendBlobRequestConditions}.
     *
     * @param requestConditions The request conditions.
     * @return The updated options.
     */
    public AppendBlobAppendBlockOptions setRequestConditions(AppendBlobRequestConditions requestConditions) {
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
    public AppendBlobAppendBlockOptions setRequestChecksumAlgorithm(StorageChecksumAlgorithm requestChecksumAlgorithm) {
        this.requestChecksumAlgorithm = requestChecksumAlgorithm;
        return this;
    }
}
