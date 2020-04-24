// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import java.util.Map;

public class PageBlobCreateOptions {
    private Long sequenceNumber;
    private BlobHttpHeaders headers;
    private Map<String, String> metadata;
    private Map<String, String> tags;
    private BlobRequestConditions requestConditions;

    public Long getSequenceNumber() {
        return sequenceNumber;
    }

    public PageBlobCreateOptions setSequenceNumber(Long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
        return this;
    }

    public BlobHttpHeaders getHeaders() {
        return headers;
    }

    public PageBlobCreateOptions setHeaders(BlobHttpHeaders headers) {
        this.headers = headers;
        return this;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public PageBlobCreateOptions setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public PageBlobCreateOptions setTags(Map<String, String> tags) {
        this.tags = tags;
        return this;
    }

    public BlobRequestConditions getRequestConditions() {
        return requestConditions;
    }

    public PageBlobCreateOptions setRequestConditions(BlobRequestConditions requestConditions) {
        this.requestConditions = requestConditions;
        return this;
    }
}
