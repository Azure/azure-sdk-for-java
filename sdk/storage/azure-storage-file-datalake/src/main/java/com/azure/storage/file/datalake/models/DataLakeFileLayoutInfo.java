// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.models;

import com.azure.core.annotation.Immutable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class contains the response information returned from the service when getting file layout.
 */
@Immutable
public final class DataLakeFileLayoutInfo {
    private final List<DataLakeFileLayoutRange> ranges;
    private final PathProperties pathProperties;

    /**
     * Constructs a {@link DataLakeFileLayoutInfo}.
     *
     * @param ranges The ranges in the file layout.
     * @param pathProperties The path properties returned with the layout.
     */
    public DataLakeFileLayoutInfo(List<DataLakeFileLayoutRange> ranges, PathProperties pathProperties) {
        this.ranges = ranges == null ? Collections.emptyList() : Collections.unmodifiableList(new ArrayList<>(ranges));
        this.pathProperties = pathProperties;
    }

    /**
     * Gets the ranges property.
     *
     * @return The ranges property.
     */
    public List<DataLakeFileLayoutRange> getRanges() {
        return ranges;
    }

    /**
     * Gets the path properties associated with this layout.
     *
     * @return The path properties, or {@code null} when unavailable.
     */
    public PathProperties getPathProperties() {
        return pathProperties;
    }
}
