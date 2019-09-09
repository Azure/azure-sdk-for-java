// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.data.tests;

<<<<<<< HEAD
import com.azure.search.data.SearchIndexClient;
import com.azure.search.data.customization.Document;
import com.azure.search.data.generated.models.IndexAction;
import com.azure.search.data.generated.models.IndexBatch;
import com.azure.search.data.models.Hotel;
import org.joda.time.DateTime;
import org.junit.Assert;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.HashMap;
=======
import com.azure.core.exception.HttpResponseException;
import com.azure.search.data.SearchIndexClient;
import com.azure.search.data.customization.Document;
import com.azure.search.data.generated.models.IndexAction;
import com.azure.search.data.generated.models.IndexActionType;
import com.azure.search.data.generated.models.IndexBatch;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

import java.util.LinkedList;
>>>>>>> master
import java.util.List;

public class IndexingSyncTests extends IndexingTestBase {
    private SearchIndexClient client;
<<<<<<< HEAD
    private int year = 2000,
        month = 1,
        day = 1,
        hour = 0,
        minute = 0,
        second = 0;
=======

    @Rule
    public ExpectedException thrown = ExpectedException.none();
>>>>>>> master

    @Override
    public void countingDocsOfNewIndexGivesZero() {
        Long actual = client.countDocuments();
        Long expected = 0L;

        Assert.assertEquals(expected, actual);
    }

    @Override
<<<<<<< HEAD
    public void dynamicDocumentDateTimesRoundTripAsUtc() throws Exception {
        HashMap<String, Object> expected = new HashMap<String, Object>();
        expected.put(HOTEL_ID_FIELD, HOTEL_ID);
        expected.put(LAST_RENOVATION_DATE_FIELD, LAST_RENOVATION_DATE);

        client.setIndexName(INDEX_NAME);
        uploadDocuments(expected);

        Document actual = client.getDocument(expected.get(HOTEL_ID_FIELD).toString());
        Assert.assertEquals(expected.get(HOTEL_ID_FIELD), actual.get(HOTEL_ID_FIELD));
        Assert.assertEquals(expected.get(LAST_RENOVATION_DATE_FIELD), actual.get(LAST_RENOVATION_DATE_FIELD));
    }

    @Override
    public void staticallyTypedDateTimesRoundTripAsUtc() throws Exception {
        Hotel expected = new Hotel()
            .hotelId(HOTEL_ID)
            .lastRenovationDate(DATE_FORMAT.parse(LAST_RENOVATION_DATE));
        System.out.println(expected.lastRenovationDate());

        client.setIndexName(INDEX_NAME);
        uploadDocuments(expected);

        Document actual = client.getDocument(expected.hotelId());
        Assert.assertEquals(expected.hotelId(), actual.get(HOTEL_ID_FIELD));
        Assert.assertEquals(expected.lastRenovationDate(), actual.get(LAST_RENOVATION_DATE_FIELD));
    }

    @Override
    protected void indexDocuments(List<IndexAction> indexActions) {
=======
    public void indexWithInvalidDocumentThrowsException() {
        thrown.expect(HttpResponseException.class);
        thrown.expectMessage("The request is invalid. Details: actions : 0: Document key cannot be missing or empty.");

        List<IndexAction> indexActions = new LinkedList<>();
        addDocumentToIndexActions(indexActions, IndexActionType.UPLOAD, new Document());
>>>>>>> master
        client.index(new IndexBatch().actions(indexActions));
    }

    @Override
    protected void initializeClient() {
        client = builderSetup().indexName(INDEX_NAME).buildClient();
    }
}
