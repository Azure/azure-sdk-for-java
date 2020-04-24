// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import java.util.Map;

public class AppendBlobCreateOptions {
    private BlobHttpHeaders headers;
    private Map<String, String> metadata;
    private Map<String, String> tags;
    private BlobRequestConditions requestConditions;

    public BlobHttpHeaders getHeaders() {
        return headers;
    }

    public AppendBlobCreateOptions setHeaders(BlobHttpHeaders headers) {
        this.headers = headers;
        return this;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public AppendBlobCreateOptions setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public AppendBlobCreateOptions setTags(Map<String, String> tags) {
        this.tags = tags;
        return this;
    }

    public BlobRequestConditions getRequestConditions() {
        return requestConditions;
    }

    public AppendBlobCreateOptions setRequestConditions(BlobRequestConditions requestConditions) {
        this.requestConditions = requestConditions;
        return this;
    }
}
