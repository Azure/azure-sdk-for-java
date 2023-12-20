// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.options;

import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.file.datalake.models.DataLakeRequestConditions;
import com.azure.storage.file.datalake.models.PathHttpHeaders;

import java.util.Map;

/**
 * Extended options that may be passed when opening an output stream to a file.
 */
public class DataLakeFileOutputStreamOptions {
    private ParallelTransferOptions parallelTransferOptions;
    private PathHttpHeaders headers;
    private Map<String, String> metadata;
    private Map<String, String> tags;
    private AccessTier tier;
    private DataLakeRequestConditions requestConditions;

    /**
     * @return {@link ParallelTransferOptions}
     */
    public ParallelTransferOptions getParallelTransferOptions() {
        return parallelTransferOptions;
    }

    /**
     * @param parallelTransferOptions {@link ParallelTransferOptions}
     * @return The updated options.
     */
    public DataLakeFileOutputStreamOptions setParallelTransferOptions(ParallelTransferOptions parallelTransferOptions) {
        this.parallelTransferOptions = parallelTransferOptions;
        return this;
    }

    /**
     * @return {@link PathHttpHeaders}
     */
    public PathHttpHeaders getHeaders() {
        return headers;
    }

    /**
     * @param headers {@link PathHttpHeaders}
     * @return The updated options.
     */
    public DataLakeFileOutputStreamOptions setHeaders(PathHttpHeaders headers) {
        this.headers = headers;
        return this;
    }

    /**
     * @return The metadata to associate with the file.
     */
    public Map<String, String> getMetadata() {
        return metadata;
    }

    /**
     * @param metadata The metadata to associate with the file.
     * @return The updated options
     */
    public DataLakeFileOutputStreamOptions setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }

    /**
     * @return The tags to associate with the file.
     */
    public Map<String, String> getTags() {
        return tags;
    }

    /**
     * @param tags The tags to associate with the file.
     * @return The updated options.
     */
    public DataLakeFileOutputStreamOptions setTags(Map<String, String> tags) {
        this.tags = tags;
        return this;
    }

    /**
     * @return {@link AccessTier}
     */
    public AccessTier getAccessTier() {
        return tier;
    }

    /**
     * @param tier {@link AccessTier}
     * @return The updated options.
     */
    public DataLakeFileOutputStreamOptions setAccessTier(AccessTier tier) {
        this.tier = tier;
        return this;
    }

    /**
     * @return {@link DataLakeRequestConditions}
     */
    public DataLakeRequestConditions getRequestConditions() {
        return requestConditions;
    }

    /**
     * @param requestConditions {@link DataLakeRequestConditions}
     * @return The updated options.
     */
    public DataLakeFileOutputStreamOptions setRequestConditions(DataLakeRequestConditions requestConditions) {
        this.requestConditions = requestConditions;
        return this;
    }
}
