// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Statistics for a given index. Statistics are collected periodically and are
 * not guaranteed to always be up-to-date.
 */
@Fluent
public final class GetIndexStatisticsResult {
    /*
     * The number of documents in the index.
     */
    @JsonProperty(value = "documentCount", required = true, access = JsonProperty.Access.WRITE_ONLY)
    private long documentCount;

    /*
     * The amount of storage in bytes consumed by the index.
     */
    @JsonProperty(value = "storageSize", required = true, access = JsonProperty.Access.WRITE_ONLY)
    private long storageSize;

    /**
     * Get the documentCount property: The number of documents in the index.
     *
     * @return the documentCount value.
     */
    public long getDocumentCount() {
        return this.documentCount;
    }

    /**
     * Get the storageSize property: The amount of storage in bytes consumed by
     * the index.
     *
     * @return the storageSize value.
     */
    public long getStorageSize() {
        return this.storageSize;
    }
}
