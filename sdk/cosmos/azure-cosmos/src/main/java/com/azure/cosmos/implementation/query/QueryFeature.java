// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.query;

public enum QueryFeature {
    None,
    Aggregate,
    CompositeAggregate,
    Distinct,
    GroupBy,
    MultipleAggregates,
    MultipleOrderBy,
    OffsetAndLimit,
    OrderBy,
    Top,
    NonValueAggregate
}
