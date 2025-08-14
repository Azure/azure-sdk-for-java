// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.options;

import com.azure.core.annotation.Fluent;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.file.datalake.models.DataLakeRequestConditions;

import java.util.Collections;
import java.util.Map;

/**
 * Extended options that may be passed when setting tags for a path.
 */
@Fluent
public class DataLakeSetTagsOptions {
    private final Map<String, String> tags;
    private DataLakeRequestConditions requestConditions;

    /**
     * Creates a new instance of {@link DataLakeSetTagsOptions}.
     *
     * @param tags Tags to associate with the path.
     * @throws NullPointerException If {@code tags} is null.
     */
    public DataLakeSetTagsOptions(Map<String, String> tags) {
        StorageImplUtils.assertNotNull("tags", tags);
        this.tags = Collections.unmodifiableMap(tags);
    }

    /**
     * Gets the tags to associate with the path.
     *
     * @return The tags to associate with the path.
     */
    public Map<String, String> getTags() {
        return tags;
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
    public DataLakeSetTagsOptions setRequestConditions(DataLakeRequestConditions requestConditions) {
        this.requestConditions = requestConditions;
        return this;
    }

}
