// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.data.tests;

import com.azure.search.data.env.SearchIndexClientTestBase;
import com.azure.search.data.generated.models.IndexAction;
import com.azure.search.data.generated.models.IndexActionType;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

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
    public abstract void indexWithInvalidDocumentThrowsException();

    @Test
    public abstract void dynamicDocumentDateTimesRoundTripAsUtc() throws Exception;

    @Test
    public abstract void staticallyTypedDateTimesRoundTripAsUtc() throws Exception;

    protected abstract void initializeClient();

    protected void addDocumentToIndexActions(List<IndexAction> indexActions, IndexActionType indexActionType, HashMap<String, Object> document) {
        indexActions.add(new IndexAction()
            .actionType(indexActionType)
            .additionalProperties(document));
    }
}
