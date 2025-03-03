// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.options;

import com.azure.core.annotation.Fluent;
import com.azure.storage.blob.models.BlobRequestConditions;

/**
 * Extended options that may be passed when getting tags for a blob.
 */
@Fluent
public class BlobGetTagsOptions {

    private BlobRequestConditions requestConditions;

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
    public BlobGetTagsOptions setRequestConditions(BlobRequestConditions requestConditions) {
        this.requestConditions = requestConditions;
        return this;
    }

}
