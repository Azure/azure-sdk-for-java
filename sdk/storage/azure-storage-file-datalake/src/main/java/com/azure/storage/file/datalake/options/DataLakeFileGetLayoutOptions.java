// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.options;

import com.azure.core.annotation.Fluent;
import com.azure.storage.file.datalake.models.DataLakeRequestConditions;
import com.azure.storage.file.datalake.models.FileRange;

/**
 * Extended options that may be passed when getting the layout of a file.
 */
@Fluent
public class DataLakeFileGetLayoutOptions {
    private FileRange range;
    private DataLakeRequestConditions requestConditions;

    /**
     * Creates a new instance of {@link DataLakeFileGetLayoutOptions}.
     */
    public DataLakeFileGetLayoutOptions() {
    }

    /**
     * Gets the range property.
     *
     * @return The range property.
     */
    public FileRange getRange() {
        return range;
    }

    /**
     * Sets the range property.
     *
     * @param range The range value to set.
     * @return The updated object
     */
    public DataLakeFileGetLayoutOptions setRange(FileRange range) {
        this.range = range == null ? null : new FileRange(range.getOffset(), range.getCount());
        return this;
    }

    /**
     * Gets the requestConditions property.
     *
     * @return The requestConditions property.
     */
    public DataLakeRequestConditions getRequestConditions() {
        return requestConditions;
    }

    /**
     * Sets the requestConditions property.
     *
     * @param requestConditions The requestConditions value to set.
     * @return The updated object
     */
    public DataLakeFileGetLayoutOptions setRequestConditions(DataLakeRequestConditions requestConditions) {
        this.requestConditions = requestConditions;
        return this;
    }
}
