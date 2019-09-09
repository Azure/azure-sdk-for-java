// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.data.tests;

import com.azure.search.data.SearchIndexClient;
import com.azure.search.data.customization.Document;
import com.azure.search.data.models.Hotel;
import org.junit.Assert;

import java.text.ParseException;

public class IndexingSyncTests extends IndexingTestBase {
    private SearchIndexClient client;

    @Override
    public void countingDocsOfNewIndexGivesZero() {
        Long actual = client.countDocuments();
        Long expected = 0L;

        Assert.assertEquals(expected, actual);
    }

    @Override
    public void canIndexStaticallyTypedDocuments() throws ParseException {
        Hotel expected = prepareStaticallyTypedHotel();
        uploadDocuments(client, INDEX_NAME, expected);

        Document result = client.getDocument(expected.hotelId());
        Hotel actual = result.as(Hotel.class);
        Assert.assertEquals(expected, actual);
    }

    @Override
    public void canIndexDynamicDocuments() {
        Document expected = prepareDynamicallyTypedHotel();
        uploadDocuments(client, INDEX_NAME, expected);

        Document result = client.getDocument(expected.get("HotelId").toString());
        Assert.assertEquals(expected, result);
    }

    @Override
    protected void initializeClient() {
        client = builderSetup().indexName(INDEX_NAME).buildClient();
    }
}
