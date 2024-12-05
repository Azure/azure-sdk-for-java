// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.options;

import com.azure.core.annotation.Fluent;
import com.azure.storage.file.share.models.ShareRequestConditions;

import java.util.Collections;
import java.util.Map;

/**
 * Extended options that may be passed when setting metadata on a share.
 */
@Fluent
public class ShareSetMetadataOptions {
    private Map<String, String> metadata;
    private ShareRequestConditions requestConditions;

    /**
     * Creates a new instance of {@link ShareSetMetadataOptions}.
     */
    public ShareSetMetadataOptions() {
    }

    /**
     * Gets the metadata to set on the share.
     *
     * @return Metadata to set on the share, if null is passed the metadata for the share is cleared.
     */
    public Map<String, String> getMetadata() {
        return metadata == null ? null : Collections.unmodifiableMap(metadata);
    }

    /**
     * Sets the metadata to set on the share.
     *
     * @param metadata Metadata to set on the share, if null is passed the metadata for the share is cleared.
     * @return The updated options.
     */
    public ShareSetMetadataOptions setMetadata(Map<String, String> metadata) {
        this.metadata = metadata == null ? null : Collections.unmodifiableMap(metadata);
        return this;
    }

    /**
     * Gets the {@link ShareRequestConditions}.
     *
     * @return {@link ShareRequestConditions}.
     */
    public ShareRequestConditions getRequestConditions() {
        return requestConditions;
    }

    /**
     * Sets the {@link ShareRequestConditions}.
     *
     * @param requestConditions {@link ShareRequestConditions}.
     * @return The updated options.
     */
    public ShareSetMetadataOptions setRequestConditions(ShareRequestConditions requestConditions) {
        this.requestConditions = requestConditions;
        return this;
    }
}
