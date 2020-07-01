// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Defines a data change detection policy that captures changes based on the
 * value of a high water mark column.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@odata.type")
@JsonTypeName("#Microsoft.Azure.Search.HighWaterMarkChangeDetectionPolicy")
@Fluent
public final class HighWaterMarkChangeDetectionPolicy extends DataChangeDetectionPolicy {
    /*
     * The name of the high water mark column.
     */
    @JsonProperty(value = "highWaterMarkColumnName", required = true)
    private String highWaterMarkColumnName;

    /**
     * Constructor of {@link HighWaterMarkChangeDetectionPolicy}.
     *
     * @param highWaterMarkColumnName The name of the high water mark column.
     */
    @JsonCreator
    public HighWaterMarkChangeDetectionPolicy(
        @JsonProperty(value = "highWaterMarkColumnName") String highWaterMarkColumnName) {
        this.highWaterMarkColumnName = highWaterMarkColumnName;
    }

    /**
     * Get the highWaterMarkColumnName property: The name of the high water
     * mark column.
     *
     * @return the highWaterMarkColumnName value.
     */
    public String getHighWaterMarkColumnName() {
        return this.highWaterMarkColumnName;
    }

}
