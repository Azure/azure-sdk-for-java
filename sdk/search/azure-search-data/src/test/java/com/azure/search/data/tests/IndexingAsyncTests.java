// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.data.tests;

import com.azure.core.exception.HttpResponseException;
import com.azure.search.data.SearchIndexAsyncClient;
import com.azure.search.data.customization.Document;
import com.azure.search.data.generated.models.DocumentIndexResult;
import com.azure.search.data.generated.models.IndexAction;
import com.azure.search.data.generated.models.IndexActionType;
import com.azure.search.data.generated.models.IndexBatch;
import com.azure.search.data.models.Hotel;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.Assert;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.HashMap;
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
    public void dynamicDocumentDateTimesRoundTripAsUtc() {
        List<HashMap<String, Object>> hotels = new ArrayList<>();
        HashMap<String, Object> expectedHotel1 = new HashMap<String, Object>();
        expectedHotel1.put(HOTEL_ID_FIELD, HOTEL_ID1);
        expectedHotel1.put(LAST_RENOVATION_DATE_FIELD, DATE_UTC);
        HashMap<String, Object> expectedHotel2 = new HashMap<String, Object>();
        expectedHotel2.put(HOTEL_ID_FIELD, HOTEL_ID2);
        expectedHotel2.put(LAST_RENOVATION_DATE_FIELD, "2010-06-27T00:00:00-00:00");
        hotels.add(expectedHotel1);
        hotels.add(expectedHotel2);

        uploadDocuments(client, INDEX_NAME, hotels);

        Mono<Document> actualHotel1 = client.getDocument(HOTEL_ID1);
        Mono<Document> actualHotel2 = client.getDocument(HOTEL_ID2);

        StepVerifier
            .create(actualHotel1)
            .assertNext(res -> {
                Assert.assertEquals(DATE_UTC, res.get(LAST_RENOVATION_DATE_FIELD));
            })
            .verifyComplete();
        StepVerifier
            .create(actualHotel2)
            .assertNext(res -> {
                Assert.assertEquals(DATE_UTC, res.get(LAST_RENOVATION_DATE_FIELD));
            })
            .verifyComplete();
    }

    @Override
    public void staticallyTypedDateTimesRoundTripAsUtc() throws Exception {
        List<Hotel> hotels = new ArrayList<>();
        Hotel expectedHotel1 = new Hotel()
            .hotelId(HOTEL_ID1)
            .lastRenovationDate(DATE_FORMAT_UTC.parse(DATE_UTC));
        Hotel expectedHotel2 = new Hotel()
            .hotelId(HOTEL_ID2)
            .lastRenovationDate(DATE_FORMAT_UNSPECIFIED_TIMEZONE.parse(DATE_UNSPECIFIED_TIMEZONE));
        hotels.add(expectedHotel1);
        hotels.add(expectedHotel2);

        uploadDocuments(client, INDEX_NAME, hotels);

        Mono<Document> actualHotel1 = client.getDocument(HOTEL_ID1);
        Mono<Document> actualHotel2 = client.getDocument(HOTEL_ID2);

        StepVerifier
            .create(actualHotel1)
            .assertNext(res -> {
                Assert.assertEquals(DATE_FORMAT_UTC.format(expectedHotel1.lastRenovationDate()), res.get(LAST_RENOVATION_DATE_FIELD));
            })
            .verifyComplete();
        StepVerifier
            .create(actualHotel2)
            .assertNext(res -> {
                Assert.assertEquals(DATE_FORMAT_UTC.format(expectedHotel2.lastRenovationDate()), res.get(LAST_RENOVATION_DATE_FIELD));
            })
            .verifyComplete();
    }

    @Override
    protected void initializeClient() {
        client = builderSetup().indexName(INDEX_NAME).buildAsyncClient();
    }
}
