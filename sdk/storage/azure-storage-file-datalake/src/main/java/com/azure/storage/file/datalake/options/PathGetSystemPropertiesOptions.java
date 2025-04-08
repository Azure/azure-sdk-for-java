// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.options;

import com.azure.core.annotation.Fluent;
import com.azure.storage.file.datalake.DataLakePathClient;
import com.azure.storage.file.datalake.models.DataLakeRequestConditions;

/**
 * Parameters when calling getSystemProperties on {@link DataLakePathClient}
 */
@Fluent
public final class PathGetSystemPropertiesOptions {
    private DataLakeRequestConditions requestConditions;

    /**
     * Creates a new instance of {@link PathGetSystemPropertiesOptions}.
     */
    public PathGetSystemPropertiesOptions() {

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
    public PathGetSystemPropertiesOptions setRequestConditions(DataLakeRequestConditions requestConditions) {
        this.requestConditions = requestConditions;
        return this;
    }

}
