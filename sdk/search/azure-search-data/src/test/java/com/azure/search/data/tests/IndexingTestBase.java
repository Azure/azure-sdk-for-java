// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.data.tests;

import com.azure.search.data.env.SearchIndexClientTestBase;
import com.azure.search.data.generated.models.IndexingResult;

import org.junit.Assert;
import org.junit.Test;

import com.azure.search.data.generated.models.IndexAction;
import com.azure.search.data.generated.models.IndexActionType;

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

    @Test
    public abstract void canIndexWithPascalCaseFields();

    @Test
    public abstract void indexWithInvalidDocumentThrowsException();

    protected abstract void initializeClient();

    protected void assertIndexActionSucceeded(String key, IndexingResult result, int expectedStatusCode) {
        Assert.assertEquals(key, result.key());
        Assert.assertTrue(result.succeeded());
        Assert.assertNull(result.errorMessage());
        Assert.assertEquals(expectedStatusCode, result.statusCode());
    }

    protected void addDocumentToIndexActions(List<IndexAction> indexActions, IndexActionType indexActionType, Map<String, Object> document) {
        indexActions.add(new IndexAction()
            .actionType(indexActionType)
            .additionalProperties(document));
    }
}
