// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.options;

import com.azure.core.annotation.Fluent;
import com.azure.storage.file.datalake.models.DataLakeRequestConditions;

/**
 * Extended options that may be passed when getting tags for a path.
 */
@Fluent
public class DataLakeGetTagsOptions {
    private DataLakeRequestConditions requestConditions;

    /**
     * Creates a new instance of {@link DataLakeGetTagsOptions}.
     */
    public DataLakeGetTagsOptions() {
    }

    /**
     * Gets the {@link DataLakeRequestConditions}.
     *
     * @return {@link DataLakeRequestConditions}
     */
    public DataLakeRequestConditions getRequestConditions() {
        return requestConditions;
    }

    /**
     * Sets the {@link DataLakeRequestConditions}.
     *
     * @param requestConditions {@link DataLakeRequestConditions}
     * @return The updated options.
     */
    public DataLakeGetTagsOptions setRequestConditions(DataLakeRequestConditions requestConditions) {
        this.requestConditions = requestConditions;
        return this;
    }

}
