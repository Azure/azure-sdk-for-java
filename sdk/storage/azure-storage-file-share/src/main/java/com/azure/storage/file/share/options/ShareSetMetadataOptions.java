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
     * @return Metadata to set on the share, if null is passed the metadata for the share is cleared.
     */
    public Map<String, String> getMetadata() {
        return metadata == null ? null : Collections.unmodifiableMap(metadata);
    }

    /**
     * @param metadata Metadata to set on the share, if null is passed the metadata for the share is cleared.
     * @return The updated options.
     */
    public ShareSetMetadataOptions setMetadata(Map<String, String> metadata) {
        this.metadata = metadata == null ? null : Collections.unmodifiableMap(metadata);
        return this;
    }

    /**
     * @return {@link ShareRequestConditions}.
     */
    public ShareRequestConditions getRequestConditions() {
        return requestConditions;
    }

    /**
     * @param requestConditions {@link ShareRequestConditions}.
     * @return The updated options.
     */
    public ShareSetMetadataOptions setRequestConditions(ShareRequestConditions requestConditions) {
        this.requestConditions = requestConditions;
        return this;
    }
}
