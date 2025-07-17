// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.models;

import io.clientcore.core.http.paging.PagingOptions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PagingOptionsTests {

    @Test
    public void testPagingOptions() {
        final long pageSize = 100L;
        final long pageIndex = 1L;
        final long offset = 50L;
        final String continuationToken = "continuation_token";

        PagingOptions pagingOptions = new PagingOptions().setPageSize(pageSize)
            .setPageIndex(pageIndex)
            .setOffset(offset)
            .setContinuationToken(continuationToken);

        Assertions.assertEquals(pageSize, pagingOptions.getPageSize());
        Assertions.assertEquals(pageIndex, pagingOptions.getPageIndex());
        Assertions.assertEquals(offset, pagingOptions.getOffset());
        Assertions.assertEquals(continuationToken, pagingOptions.getContinuationToken());
    }
}
