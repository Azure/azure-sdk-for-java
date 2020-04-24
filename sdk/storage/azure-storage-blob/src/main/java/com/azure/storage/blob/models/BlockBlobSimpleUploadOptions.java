// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import java.util.Map;

public class BlockBlobSimpleUploadOptions {
    private BlobHttpHeaders headers;
    private Map<String, String> metadata;
    private Map<String, String> tags;
    private AccessTier tier;
    private byte[] contentMd5;
    private BlobRequestConditions requestConditions;

    public BlobHttpHeaders getHeaders() {
        return headers;
    }

    public BlockBlobSimpleUploadOptions setHeaders(BlobHttpHeaders headers) {
        this.headers = headers;
        return this;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public BlockBlobSimpleUploadOptions setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public BlockBlobSimpleUploadOptions setTags(Map<String, String> tags) {
        this.tags = tags;
        return this;
    }

    public AccessTier getTier() {
        return tier;
    }

    public BlockBlobSimpleUploadOptions setTier(AccessTier tier) {
        this.tier = tier;
        return this;
    }

    public byte[] getContentMd5() {
        return contentMd5;
    }

    public BlockBlobSimpleUploadOptions setContentMd5(byte[] contentMd5) {
        this.contentMd5 = contentMd5;
        return this;
    }

    public BlobRequestConditions getRequestConditions() {
        return requestConditions;
    }

    public BlockBlobSimpleUploadOptions setRequestConditions(BlobRequestConditions requestConditions) {
        this.requestConditions = requestConditions;
        return this;
    }
}
