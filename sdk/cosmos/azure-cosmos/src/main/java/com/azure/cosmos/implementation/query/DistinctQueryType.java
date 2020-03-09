// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.query;

public enum DistinctQueryType {
    // This means that the query does not have DISTINCT.
    None,
    // This means that the query has DISTINCT, but it's not ordered perfectly.
    Unordered,
    // This means that the query has DISTINCT, and it is ordered perfectly.
    Ordered,
}
