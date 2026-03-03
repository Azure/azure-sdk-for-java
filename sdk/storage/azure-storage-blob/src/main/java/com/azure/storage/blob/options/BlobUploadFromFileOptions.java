// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.options;

import com.azure.core.annotation.Fluent;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.common.implementation.contentvalidation.StorageChecksumAlgorithm;

import java.util.Map;

/**
 * Extended options that may be passed when uploading a blob from a file.
 */
@Fluent
public class BlobUploadFromFileOptions {
    private final String filePath;
    private ParallelTransferOptions parallelTransferOptions;
    private BlobHttpHeaders headers;
    private Map<String, String> metadata;
    private Map<String, String> tags;
    private AccessTier tier;
    private BlobRequestConditions requestConditions;
    private StorageChecksumAlgorithm requestChecksumAlgorithm;

    /**
     * Constructs a {@link BlobUploadFromFileOptions}.
     *
     * @param filePath Path of the file to upload.
     * @throws NullPointerException If {@code filePath} is null.
     */
    public BlobUploadFromFileOptions(String filePath) {
        StorageImplUtils.assertNotNull("filePath", filePath);
        this.filePath = filePath;
    }

    /**
     * Gets the path of the file to upload.
     *
     * @return The path of the file to upload
     */
    public String getFilePath() {
        return filePath;
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
    public BlobUploadFromFileOptions setParallelTransferOptions(ParallelTransferOptions parallelTransferOptions) {
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
     * @return The updated {@code AppendBlobCreateOptions}
     */
    public BlobUploadFromFileOptions setHeaders(BlobHttpHeaders headers) {
        this.headers = headers;
        return this;
    }

    /**
     * Gets the metadata to associate with the blob.
     *
     * @return The metadata to associate with the blob.
     */
    public Map<String, String> getMetadata() {
        return metadata;
    }

    /**
     * Sets the metadata to associate with the blob.
     *
     * @param metadata The metadata to associate with the blob.
     * @return The updated options
     */
    public BlobUploadFromFileOptions setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }

    /**
     * Gets the tags to associate with the blob.
     *
     * @return The tags to associate with the blob.
     */
    public Map<String, String> getTags() {
        return tags;
    }

    /**
     * Sets the tags to associate with the blob.
     *
     * @param tags The tags to associate with the blob.
     * @return The updated options.
     */
    public BlobUploadFromFileOptions setTags(Map<String, String> tags) {
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
    public BlobUploadFromFileOptions setTier(AccessTier tier) {
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
    public BlobUploadFromFileOptions setRequestConditions(BlobRequestConditions requestConditions) {
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
     * Sets the algorithm to use for request content validation. When set to {@link StorageChecksumAlgorithm#AUTO} or
     * {@link StorageChecksumAlgorithm#CRC64}, the SDK will compute and send checksums for upload validation.
     * Default is {@link StorageChecksumAlgorithm#NONE}.
     *
     * @param requestChecksumAlgorithm The request checksum algorithm.
     * @return The updated options.
     */
    public BlobUploadFromFileOptions setRequestChecksumAlgorithm(StorageChecksumAlgorithm requestChecksumAlgorithm) {
        this.requestChecksumAlgorithm = requestChecksumAlgorithm;
        return this;
    }
}
