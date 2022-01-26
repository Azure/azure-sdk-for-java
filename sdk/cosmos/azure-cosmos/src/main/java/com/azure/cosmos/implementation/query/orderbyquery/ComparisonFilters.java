// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.query.orderbyquery;

public interface ComparisonFilters {
    public String lessThan();
    public String lessThanOrEqualTo();
    public String equalTo();
    public String greaterThan();
    public String greaterThanOrEqualTo();
}
