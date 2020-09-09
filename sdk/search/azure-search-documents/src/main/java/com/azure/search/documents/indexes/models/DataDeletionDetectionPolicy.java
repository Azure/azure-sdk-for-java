// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Base type for data deletion detection policies.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@odata.type",
    defaultImpl = DataDeletionDetectionPolicy.class)
@JsonTypeName("DataDeletionDetectionPolicy")
@JsonSubTypes({
    @JsonSubTypes.Type(name = "#Microsoft.Azure.Search.SoftDeleteColumnDeletionDetectionPolicy",
        value = SoftDeleteColumnDeletionDetectionPolicy.class)
})
@Fluent
public abstract class DataDeletionDetectionPolicy {
}
