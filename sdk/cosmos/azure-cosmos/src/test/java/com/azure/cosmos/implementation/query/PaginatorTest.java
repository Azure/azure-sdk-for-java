// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.query;

import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.ModelBridgeInternal;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PaginatorTest {
    @Test(groups = { "unit" }, dataProvider = "queryParams")
    public void preFetchCalculation(
        CosmosQueryRequestOptions testQueryOptions,
        int testTop,
        int testPageSize,
        int expectedPreFetchCount) {

        assertThat(
            Paginator
                .getPreFetchCount(testQueryOptions, testTop, testPageSize)
        ).isEqualTo(expectedPreFetchCount);
    }

    @DataProvider(name = "queryParams")
    public static Object[][] queryParamProvider() {

        CosmosQueryRequestOptions optionsWithBufferSizeTenThousandAndDifferentMaxItemCount =
            new CosmosQueryRequestOptions().setMaxBufferedItemCount(10_000);
        // initial continuation token
        ModelBridgeInternal
            .setQueryRequestOptionsContinuationTokenAndMaxItemCount(
                optionsWithBufferSizeTenThousandAndDifferentMaxItemCount,
                "someContinuation",
                1); // maxItemCount 1 to test that explicit page Size trumps query options

        CosmosQueryRequestOptions optionsWithBufferSizeTenThousand =
            new CosmosQueryRequestOptions().setMaxBufferedItemCount(10_000);
        // initial continuation token
        ModelBridgeInternal
            .setQueryRequestOptionsContinuationTokenAndMaxItemCount(
                optionsWithBufferSizeTenThousand,
                "someContinuation",
                1_000);

        CosmosQueryRequestOptions optionsWithMaxIntAsMaxItemCount =
            new CosmosQueryRequestOptions().setMaxBufferedItemCount(Integer.MAX_VALUE);

        return new Object[][] {
            //options, top, pageSize, expectedPreFetchCount
            { optionsWithBufferSizeTenThousand, -1, 1_000, 10 },  // top ignored
            { optionsWithBufferSizeTenThousandAndDifferentMaxItemCount, -1, 1_000, 10 }, // explicit pageSize wins
            { optionsWithBufferSizeTenThousand, 0, 1_000, 10 },   // top ignored
            { optionsWithBufferSizeTenThousand, 500, 1_000, 20 }, // effective page size is top
            { optionsWithBufferSizeTenThousand, 100, 1_000, 32 }, // effective page size is top - should result in 100
                                                                  // but max prefetch count is 32
            { optionsWithBufferSizeTenThousand, -1, -1, 32 },     // effective pageSize is at least 1
            { optionsWithBufferSizeTenThousand, -1, 0, 32 },      // effective pageSize is at least 1
            { optionsWithBufferSizeTenThousand, -1, 20_000, 1 },  // at least 1 page is buffered even when
                                                                  // maxBufferedItemCount < maxItemCount
            { optionsWithMaxIntAsMaxItemCount, -1, Integer.MAX_VALUE, 1 },  // Exactly 1 page is buffered when
                                                                            // maxBufferedItemCount == maxItemCount
        };
    }
}
