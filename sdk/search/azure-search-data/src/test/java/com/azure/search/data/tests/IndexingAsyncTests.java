// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.data.tests;

import com.azure.search.data.SearchIndexAsyncClient;
import org.junit.Assert;
import reactor.core.publisher.Mono;

public class IndexingAsyncTests extends IndexingTestBase {
    private SearchIndexAsyncClient client;

    @Override
    public void countingDocsOfNewIndexGivesZero() {
        Mono<Long> result = client.countDocuments();
        Long actual = result.block();
        Long expected = 0L;

        Assert.assertEquals(expected, actual);
    }

    @Override
    protected void initializeClient() {
        client = builderSetup().indexName(INDEX_NAME).buildAsyncClient();
    }
}
