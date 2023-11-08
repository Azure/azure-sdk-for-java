// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.options;

import com.azure.core.annotation.Fluent;
import com.azure.storage.blob.models.PublicAccessType;

import java.util.Map;

/**
 * Extended options that may be passed when creating a Blob Container.
 */
@Fluent
public class BlobContainerCreateOptions {
    private Map<String, String> metadata;
    PublicAccessType publicAccessType;

    /**
     * @return The metadata to associate with the blob.
     */
    public Map<String, String> getMetadata() {
        return metadata;
    }

    /**
     * @param metadata The metadata to associate with the blob.
     * @return The updated options
     */
    public BlobContainerCreateOptions setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }

    /**
     * @return The public access type associated with the blob.
     */
    public PublicAccessType getPublicAccessType() {
        return publicAccessType;
    }

    /**
     * @param accessType The public access type to associate with the blob.
     * @return The updated options.
     */
    public BlobContainerCreateOptions setPublicAccessType(PublicAccessType accessType) {
        publicAccessType = accessType;
        return this;
    }
}
