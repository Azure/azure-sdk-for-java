// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.data.tests;

import com.azure.search.data.SearchIndexClient;
import org.junit.Assert;

public class IndexingSyncTests extends IndexingTestBase {
    private SearchIndexClient client;

    @Override
    public void countingDocsOfNewIndexGivesZero() {
        Long actual = client.countDocuments();
        Long expected = 0L;

        Assert.assertEquals(expected, actual);
    }

    @Override
    protected void initializeClient() {
        client = builderSetup().indexName(INDEX_NAME).buildClient();
    }
}
