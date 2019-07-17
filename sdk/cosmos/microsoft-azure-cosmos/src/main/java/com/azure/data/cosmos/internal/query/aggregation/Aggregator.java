// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.query.aggregation;

public interface Aggregator {
    void aggregate(Object item);

    Object getResult();
}
