// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.options;

import com.azure.core.annotation.Fluent;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.common.implementation.StorageImplUtils;

import java.util.Collections;
import java.util.Map;

/**
 * Extended options that may be passed when setting tags for a blob.
 */
@Fluent
public class BlobSetTagsOptions {

    private final Map<String, String> tags;
    private BlobRequestConditions requestConditions;

    /**
     * @param tags Tags to associate with the blob.
     */
    public BlobSetTagsOptions(Map<String, String> tags) {
        StorageImplUtils.assertNotNull("tags", tags);
        this.tags = Collections.unmodifiableMap(tags);
    }

    /**
     * @return The tags to associate with the blob.
     */
    public Map<String, String> getTags() {
        return tags;
    }

    /**
     * @return {@link BlobRequestConditions}
     */
    public BlobRequestConditions getRequestConditions() {
        return requestConditions;
    }

    /**
     * @param requestConditions {@link BlobRequestConditions}
     * @return The updated options.
     */
    public BlobSetTagsOptions setRequestConditions(BlobRequestConditions requestConditions) {
        this.requestConditions = requestConditions;
        return this;
    }

}
