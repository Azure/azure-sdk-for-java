// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.query.orderbyquery;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;

public class ComparisonWithDefinedFilters implements ComparisonFilters {
    private final String expression;
    private final String orderByItemToString;

    public ComparisonWithDefinedFilters(String expression, String orderByItemToString) {
        checkArgument(StringUtils.isNotEmpty(expression), "Expression can not be null or empty");
        checkArgument(StringUtils.isNotEmpty(orderByItemToString), "orderByItemToString can not be null or empty");

        this.expression = expression;
        this.orderByItemToString = orderByItemToString;
    }

    public String lessThan() {
        return this.getFilterString("<");
    }

    public String lessThanOrEqualTo() {
        return this.getFilterString("<=");
    }

    public String equalTo() {
        return this.getFilterString("=");
    }

    public String greaterThan() {
        return this.getFilterString(">");
    }

    public String greaterThanOrEqualTo() {
        return this.getFilterString(">=");
    }

    private String getFilterString(String operator) {
        return String.format("%s %s %s", this.expression, operator, this.orderByItemToString);
    }
}
