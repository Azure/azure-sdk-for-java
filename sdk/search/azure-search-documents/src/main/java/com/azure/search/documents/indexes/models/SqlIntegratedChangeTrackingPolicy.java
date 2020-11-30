// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Defines a data change detection policy that captures changes using the
 * Integrated Change Tracking feature of Azure SQL Database.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@odata.type")
@JsonTypeName("#Microsoft.Azure.Search.SqlIntegratedChangeTrackingPolicy")
@Fluent
public final class SqlIntegratedChangeTrackingPolicy extends DataChangeDetectionPolicy {
}
