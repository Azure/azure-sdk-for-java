// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.data.tests;

import com.azure.search.data.SearchIndexAsyncClient;
import com.azure.search.data.generated.models.*;
import com.azure.search.data.models.Hotel;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import org.junit.Assert;

import java.util.Arrays;
import java.util.List;

import com.azure.core.exception.HttpResponseException;
import com.azure.search.data.customization.Document;
import com.azure.search.data.generated.models.DocumentIndexResult;
import io.netty.handler.codec.http.HttpResponseStatus;


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

        Hotel myHotel = new Hotel().hotelId(expectedHotelId);
        List<Hotel> toUpload = Arrays.asList(myHotel);
        Mono<DocumentIndexResult> asyncResult = client.uploadDocuments(toUpload);

        StepVerifier.create(asyncResult).assertNext(res -> {
            List<IndexingResult> result = res.results();
            this.assertIndexActionSucceeded(expectedHotelId, result.get(0), 201);
        }).verifyComplete();

        waitFor(2);

        StepVerifier.create(client.countDocuments()).
            expectNext(expectedHotelCount).
            verifyComplete();
    }

    @Override
    public void canIndexWithPascalCaseFields() {
        String expectedHotelId = "1";
        Long expectedHotelCount = 1L;

        Hotel myHotel = new Hotel().hotelId(expectedHotelId);
        myHotel.hotelName("My Pascal Hotel");
        myHotel.description("A Great Pascal Description.");
        myHotel.category("Category Pascal");
        List<Hotel> toUpload = Arrays.asList(myHotel);

        Mono<DocumentIndexResult> asyncResult = client.uploadDocuments(toUpload);

        StepVerifier.create(asyncResult).assertNext(res -> {
            List<IndexingResult> result = res.results();
            this.assertIndexActionSucceeded(expectedHotelId, result.get(0), 201);
        }).verifyComplete();

        waitFor(2);

        StepVerifier.create(client.countDocuments()).
            expectNext(expectedHotelCount).
            verifyComplete();
    }

    @Override
    public void indexWithInvalidDocumentThrowsException() {
        List<Document> toUpload = Arrays.asList(new Document());
        Mono<DocumentIndexResult> indexResult = client.uploadDocuments(toUpload);

        StepVerifier
            .create(indexResult)
            .verifyErrorSatisfies(error -> {
                Assert.assertEquals(HttpResponseException.class, error.getClass());
                Assert.assertEquals(HttpResponseStatus.BAD_REQUEST.code(), ((HttpResponseException) error).response().statusCode());
                Assert.assertTrue(error.getMessage().contains("The request is invalid. Details: actions : 0: Document key cannot be missing or empty."));
            });
    }

    @Override
    protected void initializeClient() {
        client = builderSetup().indexName(INDEX_NAME).buildAsyncClient();
    }
}
