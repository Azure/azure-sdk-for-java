// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import com.azure.core.annotation.Fluent;

import java.util.Map;

@Fluent
public class BlobUploadFromFileOptions {
    private ParallelTransferOptions parallelTransferOptions;
    private BlobHttpHeaders headers;
    private Map<String, String> metadata;
    private Map<String, String> tags;
    private AccessTier tier;
    private BlobRequestConditions requestConditions;

    public ParallelTransferOptions getParallelTransferOptions() {
        return parallelTransferOptions;
    }

    public BlobUploadFromFileOptions setParallelTransferOptions(ParallelTransferOptions parallelTransferOptions) {
        this.parallelTransferOptions = parallelTransferOptions;
        return this;
    }

    public BlobHttpHeaders getHeaders() {
        return headers;
    }

    public BlobUploadFromFileOptions setHeaders(BlobHttpHeaders headers) {
        this.headers = headers;
        return this;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public BlobUploadFromFileOptions setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public BlobUploadFromFileOptions setTags(Map<String, String> tags) {
        this.tags = tags;
        return this;
    }

    public AccessTier getTier() {
        return tier;
    }

    public BlobUploadFromFileOptions setTier(AccessTier tier) {
        this.tier = tier;
        return this;
    }

    public BlobRequestConditions getRequestConditions() {
        return requestConditions;
    }

    public BlobUploadFromFileOptions setRequestConditions(BlobRequestConditions requestConditions) {
        this.requestConditions = requestConditions;
        return this;
    }
}
