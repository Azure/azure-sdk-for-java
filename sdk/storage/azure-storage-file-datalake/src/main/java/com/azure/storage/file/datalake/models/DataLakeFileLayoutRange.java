// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.http.HttpRange;

/**
 * Represents a range in a file's layout.
 */
@Immutable
public final class DataLakeFileLayoutRange {
    private final HttpRange range;
    private final String endpoint;

    /**
     * Creates a new {@code DataLakeFileLayoutRange}.
     *
     * @param range The {@link HttpRange}.
     * @param endpoint The endpoint that contains the range.
     */
    public DataLakeFileLayoutRange(HttpRange range, String endpoint) {
        this.range = range;
        this.endpoint = endpoint;
    }

    /**
     * Gets the range property.
     *
     * @return The range property.
     */
    public HttpRange getRange() {
        return range;
    }

    /**
     * Gets the endpoint property.
     *
     * @return The endpoint property.
     */
    public String getEndpoint() {
        return endpoint;
    }
}
