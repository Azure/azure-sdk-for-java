// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.data.tests;

import com.azure.search.data.env.SearchIndexClientTestBase;
import com.azure.search.data.generated.models.IndexAction;
import com.azure.search.data.generated.models.IndexingResult;
import com.azure.search.data.models.Hotel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class IndexingTestBase extends SearchIndexClientTestBase {
    protected static final String INDEX_NAME = "hotels";

    @Override
    protected void beforeTest() {
        super.beforeTest();
        initializeClient();
    }

    @Test
    public abstract void countingDocsOfNewIndexGivesZero();

    @Test
    public abstract void indexDoesNotThrowWhenAllActionsSucceed();

    protected abstract void initializeClient();
    protected abstract List<IndexingResult> indexDocuments(List<IndexAction> indexActions);

    protected void AssertIndexActionSucceeded(String key, IndexingResult result, int expectedStatusCode)
    {
        Assert.assertEquals(key, result.key());
        Assert.assertTrue(result.succeeded());
        Assert.assertNull(result.errorMessage());
        Assert.assertEquals(expectedStatusCode, result.statusCode());
    }
}
