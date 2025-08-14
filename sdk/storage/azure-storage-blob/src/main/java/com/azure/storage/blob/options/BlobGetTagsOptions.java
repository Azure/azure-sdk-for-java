// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.options;

import com.azure.core.annotation.Fluent;
import com.azure.storage.blob.models.BlobModifiedAccessConditions;
import com.azure.storage.blob.models.BlobRequestConditions;

/**
 * Extended options that may be passed when getting tags for a blob.
 */
@Fluent
public class BlobGetTagsOptions {
    private BlobRequestConditions requestConditions;
    private BlobModifiedAccessConditions blobModifiedAccessConditions;

    /**
     * Creates a new instance of {@link BlobGetTagsOptions}.
     */
    public BlobGetTagsOptions() {
    }

    /**
     * Gets the {@link BlobRequestConditions}.
     *
     * @return {@link BlobRequestConditions}
     */
    public BlobRequestConditions getRequestConditions() {
        return requestConditions;
    }

    /**
     * Sets the {@link BlobRequestConditions}.
     *
     * @param requestConditions {@link BlobRequestConditions}
     * @return The updated options.
     */
    public BlobGetTagsOptions setRequestConditions(BlobRequestConditions requestConditions) {
        this.requestConditions = requestConditions;
        return this;
    }

    /**
     * Gets the {@link BlobModifiedAccessConditions}. Although similar to {@link BlobRequestConditions}, these conditions
     * apply to the blob associated with the tag rather than the tag(s) itself.
     *
     * @return {@link BlobModifiedAccessConditions}
     */
    public BlobModifiedAccessConditions getBlobModifiedAccessConditions() {
        return blobModifiedAccessConditions;
    }

    /**
     * Sets the {@link BlobModifiedAccessConditions}. Although similar to {@link BlobRequestConditions}, these conditions
     * apply to the blob associated with the tag rather than the tag(s) itself.
     *
     * @param blobModifiedAccessConditions {@link BlobModifiedAccessConditions}
     * @return The updated options.
     */
    public BlobGetTagsOptions setBlobModifiedAccessConditions(BlobModifiedAccessConditions blobModifiedAccessConditions) {
        this.blobModifiedAccessConditions = blobModifiedAccessConditions;
        return this;
    }

}
