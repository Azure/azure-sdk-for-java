// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.options;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobImmutabilityPolicy;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.common.implementation.StorageImplUtils;
import reactor.core.publisher.Flux;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Map;

/**
 * Extended options that may be passed when uploading a Block Blob in parallel.
 */
@Fluent
public class BlobParallelUploadOptions {
    private final Flux<ByteBuffer> dataFlux;
    private final InputStream dataStream;
    private final Long length;
    private ParallelTransferOptions parallelTransferOptions;
    private BlobHttpHeaders headers;
    private Map<String, String> metadata;
    private Map<String, String> tags;
    private AccessTier tier;
    private BlobRequestConditions requestConditions;
    private boolean computeMd5;
    private Duration timeout;
    private BlobImmutabilityPolicy immutabilityPolicy;
    private Boolean legalHold;

    /**
     * Constructs a new {@link BlobParallelUploadOptions}.
     *
     * @param dataFlux The data to write to the blob. Unlike other upload methods, this method does not require that
     * the {@code Flux} be replayable. In other words, it does not have to support multiple subscribers and is not
     * expected to produce the same values across subscriptions.
     */
    public BlobParallelUploadOptions(Flux<ByteBuffer> dataFlux) {
        StorageImplUtils.assertNotNull("dataFlux", dataFlux);
        this.dataFlux = dataFlux;
        this.dataStream = null;
        this.length = null;
    }

    /**
     * Constructs a new {@link BlobParallelUploadOptions}.
     *
     * Use {@link #BlobParallelUploadOptions(InputStream)} instead to supply an InputStream without knowing the exact
     * length beforehand.
     *
     * @param dataStream The data to write to the blob.
     * @param length The exact length of the data. It is important that this value match precisely the length of the
     * data provided in the {@link InputStream}.
     * @deprecated length is no longer necessary; use {@link #BlobParallelUploadOptions(InputStream)} instead.
     */
    @Deprecated
    public BlobParallelUploadOptions(InputStream dataStream, long length) {
        this(dataStream, Long.valueOf(length));
    }

    /**
     * Constructs a new {@link BlobParallelUploadOptions}.
     *
     * @param dataStream The data to write to the blob.
     */
    public BlobParallelUploadOptions(InputStream dataStream) {
        this(dataStream, null);
    }

    /**
     * Common constructor for building options from InputStream.
     *
     * @param dataStream The data to write to the blob.
     * @param length Optional known length of the data, affects reactive behavior for backwards compatibility.
     */
    private BlobParallelUploadOptions(InputStream dataStream, Long length) {
        StorageImplUtils.assertNotNull("dataStream", dataStream);
        if (length != null) {
            StorageImplUtils.assertInBounds("length", length, 0, Long.MAX_VALUE);
        }
        this.dataStream = dataStream;
        this.dataFlux = null;
        this.length = length;
    }

    /**
     * Constructs a new {@code BlobParallelUploadOptions}.
     *
     * @param data The data to write to the blob.
     */
    public BlobParallelUploadOptions(BinaryData data) {
        StorageImplUtils.assertNotNull("data", data);
        this.dataFlux = data.toFluxByteBuffer();
        this.dataStream = null;
        this.length = null;
    }

    /**
     * Gets the data source.
     *
     * @return The data to write to the blob.
     */
    public Flux<ByteBuffer> getDataFlux() {
        return this.dataFlux;
    }

    /**
     * Gets the data source.
     *
     * @return The data to write to the blob.
     */
    public InputStream getDataStream() {
        return this.dataStream;
    }

    /**
     * Gets the length of the data.
     *
     * @return The exact length of the data. It is important that this value match precisely the length of the
     * data provided in the {@link InputStream}.
     * @deprecated use {@link #getOptionalLength()} to have safe access to a length that will not always exist.
     */
    @Deprecated
    public long getLength() {
        return length;
    }

    /**
     * Gets the length of the data.
     *
     * @return The exact length of the data. It is important that this value match precisely the length of the
     * data provided in the {@link InputStream}.
     */
    public Long getOptionalLength() {
        return length;
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
    public BlobParallelUploadOptions setParallelTransferOptions(ParallelTransferOptions parallelTransferOptions) {
        this.parallelTransferOptions = parallelTransferOptions;
        return this;
    }

    /**
     * Gets the {@link BlobHttpHeaders}.
     *
     * @return {@link BlobHttpHeaders}
     */
    public BlobHttpHeaders getHeaders() {
        return headers;
    }

    /**
     * Sets the {@link BlobHttpHeaders}.
     *
     * @param headers {@link BlobHttpHeaders}
     * @return The updated options
     */
    public BlobParallelUploadOptions setHeaders(BlobHttpHeaders headers) {
        this.headers = headers;
        return this;
    }

    /**
     * Gets the metadata.
     *
     * @return The metadata to associate with the blob.
     */
    public Map<String, String> getMetadata() {
        return metadata;
    }

    /**
     * Sets the metadata.
     *
     * @param metadata The metadata to associate with the blob.
     * @return The updated options.
     */
    public BlobParallelUploadOptions setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }

    /**
     * Get the tags.
     *
     * @return The tags to associate with the blob.
     */
    public Map<String, String> getTags() {
        return tags;
    }

    /**
     * Set the tags.
     *
     * @param tags The tags to associate with the blob.
     * @return The updated options.
     */
    public BlobParallelUploadOptions setTags(Map<String, String> tags) {
        this.tags = tags;
        return this;
    }

    /**
     * Gets the {@link AccessTier}.
     *
     * @return {@link AccessTier}
     */
    public AccessTier getTier() {
        return tier;
    }

    /**
     * Sets the {@link AccessTier}.
     *
     * @param tier {@link AccessTier}
     * @return The updated options.
     */
    public BlobParallelUploadOptions setTier(AccessTier tier) {
        this.tier = tier;
        return this;
    }

    /**
     * Gets the {@link BlobRequestConditions}.
     *
     * @return {@link BlobRequestConditions}
     */
    public BlobRequestConditions getRequestConditions() {
        return requestConditions;
    }

    /**
     * Sets the {@link BlobRequestConditions}.
     *
     * @param requestConditions {@link BlobRequestConditions}
     * @return The updated options.
     */
    public BlobParallelUploadOptions setRequestConditions(BlobRequestConditions requestConditions) {
        this.requestConditions = requestConditions;
        return this;
    }

    /**
     * @return Whether or not the library should calculate the md5 and send it for the service to verify.
     */
    public boolean isComputeMd5() {
        return computeMd5;
    }

    /**
     * Sets the computeMd5 property.
     *
     * @param computeMd5 Whether or not the library should calculate the md5 and send it for the service to
     * verify.
     * @return The updated options.
     */
    public BlobParallelUploadOptions setComputeMd5(boolean computeMd5) {
        this.computeMd5 = computeMd5;
        return this;
    }

    /**
     * Gets the timeout.
     *
     * @return An optional timeout value beyond which a {@link RuntimeException} will be raised.
     *
     * @deprecated Use {@link BlobClient#uploadWithResponse(BlobParallelUploadOptions, Duration, Context)} to
     * specify timeout.
     */
    @Deprecated
    public Duration getTimeout() {
        return this.timeout;
    }

    /**
     * Sets the timeout.
     *
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @return The updated options
     *
     * @deprecated Use {@link BlobClient#uploadWithResponse(BlobParallelUploadOptions, Duration, Context)} to
     * specify timeout.
     */
    @Deprecated
    public BlobParallelUploadOptions setTimeout(Duration timeout) {
        this.timeout = timeout;
        return this;
    }

    /**
     * @return {@link BlobImmutabilityPolicy}
     */
    public BlobImmutabilityPolicy getImmutabilityPolicy() {
        return immutabilityPolicy;
    }

    /**
     * Note that this parameter is only applicable to a blob within a container that has immutable storage with
     * versioning enabled.
     * @param immutabilityPolicy {@link BlobImmutabilityPolicy}
     * @return The updated options.
     */
    public BlobParallelUploadOptions setImmutabilityPolicy(BlobImmutabilityPolicy immutabilityPolicy) {
        this.immutabilityPolicy = immutabilityPolicy;
        return this;
    }

    /**
     * @return If a legal hold should be placed on the blob.
     */
    public Boolean isLegalHold() {
        return legalHold;
    }

    /**
     * Note that this parameter is only applicable to a blob within a container that has immutable storage with
     * versioning enabled.
     * @param legalHold Indicates if a legal hold should be placed on the blob.
     * @return The updated options.
     */
    public BlobParallelUploadOptions setLegalHold(Boolean legalHold) {
        this.legalHold = legalHold;
        return this;
    }
}
