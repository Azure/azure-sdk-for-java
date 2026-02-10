// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.batch;

import com.azure.cosmos.implementation.CosmosTransactionalBulkExecutionOptionsImpl;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.testng.AssertJUnit.fail;

public class CosmosTransactionalBulkExecutionOptionsImplTest {

    @DataProvider(name = "maxBatchesConcurrencyArgProvider")
    public static Object[][] maxBatchesConcurrencyArgProvider() {
        return new Object[][]{
            // value, is valid
            { 1, true },
            { 10, false },
            { 5, true },
            { 0, false },
            { -1, false }
        };
    }

    @Test(groups = "unit")
    public void default_options() {
        CosmosTransactionalBulkExecutionOptionsImpl options = new CosmosTransactionalBulkExecutionOptionsImpl();
        assertThat(options.getMaxBatchesConcurrency()).isEqualTo(5);
        assertThat(options.getMaxOperationsConcurrency()).isEqualTo(100);
    }

    @Test(groups = "unit", dataProvider = "maxBatchesConcurrencyArgProvider")
    public void setMaxOperationsConcurrency(int value, boolean isValid) {
        CosmosTransactionalBulkExecutionOptionsImpl options = new CosmosTransactionalBulkExecutionOptionsImpl();
        try {
            options.setMaxBatchesConcurrency(value);
            if (isValid) {
                assertThat(options.getMaxBatchesConcurrency()).isEqualTo(value);
            } else {
                fail("Should have failed for maxBatchesConcurrency " + value);
            }
        } catch (IllegalArgumentException e) {
            if (isValid) {
                fail("should be valid value for maxBatchesConcurrency");
            }
        }
    }

}
