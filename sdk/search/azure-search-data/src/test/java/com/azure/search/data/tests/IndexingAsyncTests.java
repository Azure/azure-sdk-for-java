// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.data.tests;

import com.azure.search.data.SearchIndexAsyncClient;
import com.azure.search.data.generated.models.*;
import com.azure.search.data.helpers.EntityMapper;
import com.azure.search.data.models.Hotel;
import org.junit.Assert;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
        String expectedHotelId = "1";
        Long expectedHotelCount = 1L;

        List<IndexAction> indexActions = new LinkedList<>();
        Hotel myHotel = new Hotel().hotelId("1");
        Map<String, Object> hotelMap = new EntityMapper<Hotel>().objectToMap(myHotel);

        indexActions.add(new IndexAction().actionType(IndexActionType.UPLOAD).additionalProperties(hotelMap));
        Mono<DocumentIndexResult> asyncResult = indexDocumentsAsync(indexActions);

        // TODO
        StepVerifier.create(asyncResult).assertNext(res -> {
            List<IndexingResult> result = res.results();
            this.AssertIndexActionSucceeded(expectedHotelId, result.get(0), 201);
        }).verifyComplete();

        StepVerifier.create(client.countDocuments()).expectNext(expectedHotelCount).expectComplete().verify();
    }

    @Override
    protected void initializeClient() {
        client = builderSetup().indexName(INDEX_NAME).buildAsyncClient();
    }

    protected Mono<DocumentIndexResult> indexDocumentsAsync(List<IndexAction> indexActions) {
        IndexBatch indexBatch = new IndexBatch().actions(indexActions);
        Mono<DocumentIndexResult> indexResult = client.index(indexBatch);
        return indexResult;
    }
}
