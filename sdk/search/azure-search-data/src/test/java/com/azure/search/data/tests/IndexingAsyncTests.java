// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.data.tests;

import com.azure.search.data.SearchIndexAsyncClient;
import com.azure.search.data.customization.Document;
import com.azure.search.data.models.Hotel;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class IndexingAsyncTests extends IndexingTestBase {
    private SearchIndexAsyncClient client;

    @Override
    public void countingDocsOfNewIndexGivesZero() {
        Mono<Long> result = client.countDocuments();
        Long expected = 0L;

        StepVerifier.create(result).expectNext(expected).expectComplete().verify();
    }

    @Override
    public void canIndexStaticallyTypedDocuments() throws Exception {
        Hotel expected = prepareStaticallyTypedHotel();
        uploadDocuments(client, INDEX_NAME, expected);

        Mono<Document> result = client.getDocument(expected.hotelId());

        StepVerifier.create(result)
            .expectNextMatches(resultDocument -> {
                Hotel actual = resultDocument.as(Hotel.class);
                return actual.equals(expected);
            })
            .expectComplete()
            .verify();
    }

    @Override
    public void canIndexDynamicDocuments() throws Exception {
        Document expected = prepareDynamicallyTypedHotel();
        uploadDocuments(client, INDEX_NAME, expected);

        Mono<Document> result = client.getDocument(expected.get("HotelId").toString());

        StepVerifier.create(result)
            .expectNext(expected)
            .expectComplete()
            .verify();
    }

    @Override
    protected void initializeClient() {
        client = builderSetup().indexName(INDEX_NAME).buildAsyncClient();
    }
}
