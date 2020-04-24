// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import java.util.Map;

public class BlockBlobOutputStreamOptions {
    private ParallelTransferOptions parallelTransferOptions;
    private BlobHttpHeaders headers;
    private Map<String, String> metadata;
    private Map<String, String> tags;
    private AccessTier tier;
    private BlobRequestConditions requestConditions;

    public ParallelTransferOptions getParallelTransferOptions() {
        return parallelTransferOptions;
    }

    public BlockBlobOutputStreamOptions setParallelTransferOptions(ParallelTransferOptions parallelTransferOptions) {
        this.parallelTransferOptions = parallelTransferOptions;
        return this;
    }

    public BlobHttpHeaders getHeaders() {
        return headers;
    }

    public BlockBlobOutputStreamOptions setHeaders(BlobHttpHeaders headers) {
        this.headers = headers;
        return this;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public BlockBlobOutputStreamOptions setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public BlockBlobOutputStreamOptions setTags(Map<String, String> tags) {
        this.tags = tags;
        return this;
    }

    public AccessTier getTier() {
        return tier;
    }

    public BlockBlobOutputStreamOptions setTier(AccessTier tier) {
        this.tier = tier;
        return this;
    }

    public BlobRequestConditions getRequestConditions() {
        return requestConditions;
    }

    public BlockBlobOutputStreamOptions setRequestConditions(BlobRequestConditions requestConditions) {
        this.requestConditions = requestConditions;
        return this;
    }
}
