// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.options;

import com.azure.core.annotation.Fluent;
import com.azure.storage.file.datalake.models.DataLakeRequestConditions;

/**
 * Extended options that may be passed when deleting a datalake resource.
 */
@Fluent
public class DataLakePathDeleteOptions {

    private boolean isRecursive;
    private DataLakeRequestConditions requestConditions;

    /**
     * Constructs a {@link DataLakePathDeleteOptions}.
     */
    public DataLakePathDeleteOptions() {
    }

    /**
     * @return whether everything under the resource should be deleted recursively
     */
    public boolean getIsRecursive() {
        return isRecursive;
    }

    /**
     * Sets the permissions.
     *
     * @param recursive whether resource should be deleted recursively.
     * @return the updated options.
     */
    public DataLakePathDeleteOptions setIsRecursive(boolean recursive) {
        isRecursive = recursive;
        return this;
    }

    /**
     * Gets the request conditions.
     *
     * @return the request conditions.
     */
    public DataLakeRequestConditions getRequestConditions() {
        return requestConditions;
    }

    /**
     * Sets the request conditions.
     *
     * @param requestConditions The request conditions.
     * @return the updated FileQueryOptions object.
     */
    public DataLakePathDeleteOptions setRequestConditions(DataLakeRequestConditions requestConditions) {
        this.requestConditions = requestConditions;
        return this;
    }

}
