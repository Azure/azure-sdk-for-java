// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.query;

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class OrderByContinuationLiteralTest {
    @Test(groups = {"unit"})
    public void escapesStringValuesInContinuationPredicates() {
        assertThat(OrderByDocumentQueryExecutionContext.getOrderByItemString("a\"b\\c\n\t"))
            .isEqualTo("\"a\\\"b\\\\c\\n\\t\"");
    }
}
