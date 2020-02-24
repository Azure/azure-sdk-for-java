// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal;

/**
 * A client query compatibility mode when making query request in the Azure Cosmos DB database service. Can be used
 * to force a specific query request format.
 */
public enum QueryCompatibilityMode {
    Default,
    Query,
    SqlQuery
}