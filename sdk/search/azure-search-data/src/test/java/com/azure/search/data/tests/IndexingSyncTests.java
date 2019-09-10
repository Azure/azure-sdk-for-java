// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.data.tests;

import com.azure.core.exception.HttpResponseException;
import com.azure.search.data.SearchIndexClient;
import com.azure.search.data.customization.Document;
import com.azure.search.data.generated.models.IndexAction;
import com.azure.search.data.generated.models.IndexActionType;
import com.azure.search.data.generated.models.IndexBatch;
import com.azure.search.data.models.Hotel;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class IndexingSyncTests extends IndexingTestBase {
    private SearchIndexClient client;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Override
    public void countingDocsOfNewIndexGivesZero() {
        Long actual = client.countDocuments();
        Long expected = 0L;

        Assert.assertEquals(expected, actual);
    }

    @Override
    public void indexWithInvalidDocumentThrowsException() {
        thrown.expect(HttpResponseException.class);
        thrown.expectMessage("The request is invalid. Details: actions : 0: Document key cannot be missing or empty.");

        List<IndexAction> indexActions = new LinkedList<>();
        addDocumentToIndexActions(indexActions, IndexActionType.UPLOAD, new Document());
        client.index(new IndexBatch().actions(indexActions));
    }

    public void dynamicDocumentDateTimesRoundTripAsUtc() throws Exception {
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

        Document actualHotel1 = client.getDocument(HOTEL_ID1);
        Document actualHotel2 = client.getDocument(HOTEL_ID2);

        Assert.assertEquals(DATE_UTC, actualHotel1.get(LAST_RENOVATION_DATE_FIELD));
        Assert.assertEquals(DATE_UTC, actualHotel2.get(LAST_RENOVATION_DATE_FIELD));
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

        Document actualHotel1 = client.getDocument(HOTEL_ID1);
        Document actualHotel2 = client.getDocument(HOTEL_ID2);

        Assert.assertEquals(DATE_FORMAT_UTC.format(expectedHotel1.lastRenovationDate()), actualHotel1.get(LAST_RENOVATION_DATE_FIELD));
        Assert.assertEquals(DATE_FORMAT_UTC.format(expectedHotel2.lastRenovationDate()), actualHotel2.get(LAST_RENOVATION_DATE_FIELD));
    }

    @Override
    protected void initializeClient() {
        client = builderSetup().indexName(INDEX_NAME).buildClient();
    }
}
