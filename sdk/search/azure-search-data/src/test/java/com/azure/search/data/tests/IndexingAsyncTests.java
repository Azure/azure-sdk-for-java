// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.data.tests;

import com.azure.search.data.SearchIndexAsyncClient;
import com.azure.search.data.generated.models.IndexAction;
import com.azure.search.data.generated.models.IndexingResult;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

public class IndexingAsyncTests extends IndexingTestBase {
    private SearchIndexAsyncClient client;

    @Override
    public void countingDocsOfNewIndexGivesZero() {
        Mono<Long> result = client.countDocuments();
        Long expected = 0L;

        StepVerifier.create(result).expectNext(expected).expectComplete().verify();
    }

    @Override
    public void indexDoesNotThrowWhenAllActionsSucceed() {

    }

    @Override
    protected void initializeClient() {
        client = builderSetup().indexName(INDEX_NAME).buildAsyncClient();
    }

    @Override
    protected List<IndexingResult> indexDocuments(List<IndexAction> indexActions) {
        return null;
    }
}
