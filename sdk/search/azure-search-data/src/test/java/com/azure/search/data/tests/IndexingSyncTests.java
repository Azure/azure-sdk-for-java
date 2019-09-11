// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.data.tests;

import com.azure.search.data.SearchIndexClient;
import com.azure.search.data.generated.models.*;
import com.azure.search.data.models.Hotel;
import org.junit.Assert;

import java.util.Arrays;
import java.util.List;

import com.azure.core.exception.HttpResponseException;
import com.azure.search.data.customization.Document;
import org.junit.Rule;
import org.junit.rules.ExpectedException;


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
    public void indexDoesNotThrowWhenAllActionsSucceed() {
        String expectedHotelId = "1";
        Long expectedHotelCount = 1L;

        Hotel myHotel = new Hotel().hotelId(expectedHotelId);
        List<Hotel> toUpload = Arrays.asList(myHotel);

        List<IndexingResult> result = client.uploadDocuments(toUpload).results();
        this.assertIndexActionSucceeded(expectedHotelId, result.get(0), 201);

        waitFor(2);
        Assert.assertEquals(expectedHotelCount, client.countDocuments());
    }

    @Override
    public void canIndexWithPascalCaseFields() {
        String expectedHotelId = "1";
        Long expectedHotelCount = 1L;

        Hotel myHotel =
            new Hotel().hotelId(expectedHotelId).
                hotelName("My Pascal Hotel").
                description("A Great Pascal Description.").
                category("Category Pascal");
        List<Hotel> toUpload = Arrays.asList(myHotel);

        List<IndexingResult> result = client.uploadDocuments(toUpload).results();
        this.assertIndexActionSucceeded(expectedHotelId, result.get(0), 201);

        waitFor(2);
        Assert.assertEquals(expectedHotelCount, client.countDocuments());
    }

    public void indexWithInvalidDocumentThrowsException() {
        thrown.expect(HttpResponseException.class);
        thrown.expectMessage("The request is invalid. Details: actions : 0: Document key cannot be missing or empty.");

        List<Document> toUpload = Arrays.asList(new Document());
        client.uploadDocuments(toUpload);
    }

    @Override
    protected void initializeClient() {
        client = builderSetup().indexName(INDEX_NAME).buildClient();
    }
}
