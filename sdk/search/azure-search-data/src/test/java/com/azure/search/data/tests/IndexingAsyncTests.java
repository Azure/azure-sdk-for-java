// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.data.tests;

import com.azure.core.exception.HttpResponseException;
import com.azure.search.data.SearchIndexAsyncClient;
import com.azure.search.data.customization.Document;
import com.azure.search.data.customization.IndexingAction;
import com.azure.search.data.generated.models.*;
import com.azure.search.data.models.Hotel;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.text.ParseException;

import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class IndexingAsyncTests extends IndexingTestBase {
    private SearchIndexAsyncClient client;

    @Override
    public void countingDocsOfNewIndexGivesZero() {
        Mono<Long> result = client.countDocuments();
        Long expected = 0L;

        StepVerifier.create(result).expectNext(expected).expectComplete().verify();
    }

    @Override
    public void canIndexStaticallyTypedDocuments() throws ParseException {
        Hotel hotel1 = prepareStaticallyTypedHotel("1");
        Hotel hotel2 = prepareStaticallyTypedHotel("2");
        Hotel hotel3 = prepareStaticallyTypedHotel("3");
        Hotel nonExistingHotel = prepareStaticallyTypedHotel("nonExistingHotel");

        IndexAction uploadAction = IndexingAction.upload(hotel1);
        IndexAction deleteAction = IndexingAction.delete("HotelId", "randomId");
        IndexAction mergeNonExistingAction = IndexingAction.merge(nonExistingHotel);
        IndexAction mergeOrUploadAction = IndexingAction.mergeOrUpload(hotel3);
        IndexAction uploadAction2 = IndexingAction.upload(hotel2);

        IndexBatch indexBatch = new IndexBatch().actions(Arrays.asList(
            uploadAction,
            deleteAction,
            mergeNonExistingAction,
            mergeOrUploadAction,
            uploadAction2
        ));

        StepVerifier.create(client.index(indexBatch))
            .expectNextMatches(documentIndexResult -> {
                List<IndexingResult> results = documentIndexResult.results();

                AssertSuccessfulIndexResult(results.get(0), "1", 201);
                AssertSuccessfulIndexResult(results.get(1), "randomId", 200);
                AssertFailedIndexResult(results.get(2), "nonExistingHotel", 404, "Document not found.");
                AssertSuccessfulIndexResult(results.get(3), "3", 201);
                AssertSuccessfulIndexResult(results.get(4), "2", 201);

                return results.size() == indexBatch.actions().size();
            })
            .expectComplete()
            .verify();

        StepVerifier.create(client.getDocument(hotel1.hotelId()))
            .expectNextMatches(result -> {
                Hotel actual = result.as(Hotel.class);
                return actual.equals(hotel1);
            })
            .expectComplete()
            .verify();

        StepVerifier.create(client.getDocument(hotel2.hotelId()))
            .expectNextMatches(result -> {
                Hotel actual = result.as(Hotel.class);
                return actual.equals(hotel2);
            })
            .expectComplete()
            .verify();

        StepVerifier.create(client.getDocument(hotel3.hotelId()))
            .expectNextMatches(result -> {
                Hotel actual = result.as(Hotel.class);
                return actual.equals(hotel3);
            })
            .expectComplete()
            .verify();
    }

    @Override
    public void canIndexDynamicDocuments() {
        Document hotel1 = prepareDynamicallyTypedHotel("1");
        Document hotel2 = prepareDynamicallyTypedHotel("2");
        Document hotel3 = prepareDynamicallyTypedHotel("3");
        Document nonExistingHotel = prepareDynamicallyTypedHotel("nonExistingHotel");

        IndexAction uploadAction = IndexingAction.upload(hotel1);
        IndexAction deleteAction = IndexingAction.delete("HotelId", "randomId");
        IndexAction mergeNonExistingAction = IndexingAction.merge(nonExistingHotel);
        IndexAction mergeOrUploadAction = IndexingAction.mergeOrUpload(hotel3);
        IndexAction uploadAction2 = IndexingAction.upload(hotel2);

        IndexBatch indexBatch = new IndexBatch().actions(Arrays.asList(
            uploadAction,
            deleteAction,
            mergeNonExistingAction,
            mergeOrUploadAction,
            uploadAction2
        ));

        StepVerifier.create(client.index(indexBatch))
            .expectNextMatches(documentIndexResult -> {
                List<IndexingResult> results = documentIndexResult.results();

                AssertSuccessfulIndexResult(results.get(0), "1", 201);
                AssertSuccessfulIndexResult(results.get(1), "randomId", 200);
                AssertFailedIndexResult(results.get(2), "nonExistingHotel", 404, "Document not found.");
                AssertSuccessfulIndexResult(results.get(3), "3", 201);
                AssertSuccessfulIndexResult(results.get(4), "2", 201);

                return results.size() == indexBatch.actions().size();
            })
            .expectComplete()
            .verify();

        StepVerifier.create(client.getDocument(hotel1.get("HotelId").toString()))
            .expectNext(hotel1)
            .expectComplete()
            .verify();

        StepVerifier.create(client.getDocument(hotel2.get("HotelId").toString()))
            .expectNext(hotel2)
            .expectComplete()
            .verify();

        StepVerifier.create(client.getDocument(hotel3.get("HotelId").toString()))
            .expectNext(hotel3)
            .expectComplete()
            .verify();
    }

    @Override
    public void indexWithInvalidDocumentThrowsException() {
        List<IndexAction> indexActions = new LinkedList<>();
        addDocumentToIndexActions(indexActions, IndexActionType.UPLOAD, new Document());
        Mono<DocumentIndexResult> indexResult = client.index(new IndexBatch().actions(indexActions));

        StepVerifier
            .create(indexResult)
            .verifyErrorSatisfies(error -> {
                assertEquals(HttpResponseException.class, error.getClass());
                assertEquals(HttpResponseStatus.BAD_REQUEST.code(), ((HttpResponseException) error).response().statusCode());
                assertTrue(error.getMessage().contains("The request is invalid. Details: actions : 0: Document key cannot be missing or empty."));
            });
    }

    @Override
    protected void initializeClient() {
        client = builderSetup().indexName(INDEX_NAME).buildAsyncClient();
    }
}
