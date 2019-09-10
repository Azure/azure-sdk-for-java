// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.data.tests;

import com.azure.search.data.SearchIndexClient;
import com.azure.search.data.generated.models.*;
import com.azure.search.data.helpers.EntityMapper;
import com.azure.search.data.models.Hotel;
import org.junit.Assert;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.azure.core.exception.HttpResponseException;
import com.azure.search.data.customization.Document;
import com.azure.search.data.generated.models.IndexAction;
import com.azure.search.data.generated.models.IndexActionType;
import com.azure.search.data.generated.models.IndexBatch;
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

        List<IndexAction> indexActions = new LinkedList<>();
        Hotel myHotel = new Hotel().hotelId(expectedHotelId);
        Map<String, Object> hotelMap = new EntityMapper<Hotel>().objectToMap(myHotel);

        addDocumentToIndexActions(indexActions, IndexActionType.UPLOAD, hotelMap);
        List<IndexingResult> result = indexDocumentsSync(indexActions);

        Assert.assertEquals(expectedHotelCount, client.countDocuments());
        this.AssertIndexActionSucceeded(expectedHotelId, result.get(0), 201);
    }

    @Override
    public void canIndexWithPascalCaseFields() {
        String expectedHotelId = "1";
        Long expectedHotelCount = 1L;

        List<IndexAction> indexActions = new LinkedList<>();
        Hotel myHotel = new Hotel().hotelId(expectedHotelId);
        myHotel.hotelName("My Pascal Hotel");
        myHotel.description("A Great Pascal Description.");
        myHotel.category("Category Pascal");

        Map<String, Object> hotelMap = new EntityMapper<Hotel>().objectToMap(myHotel);

        addDocumentToIndexActions(indexActions, IndexActionType.UPLOAD, hotelMap);
        List<IndexingResult> result = indexDocumentsSync(indexActions);

        Assert.assertEquals(expectedHotelCount, client.countDocuments());
        this.AssertIndexActionSucceeded(expectedHotelId, result.get(0), 201);
    }

    protected List<IndexingResult> indexDocumentsSync(List<IndexAction> indexActions) {
        IndexBatch indexBatch = new IndexBatch().actions(indexActions);
        DocumentIndexResult indexResult = client.index(indexBatch);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Assert.fail(e.getMessage());
        }

        return indexResult.results();
    }

    public void indexWithInvalidDocumentThrowsException() {
        thrown.expect(HttpResponseException.class);
        thrown.expectMessage("The request is invalid. Details: actions : 0: Document key cannot be missing or empty.");

        List<IndexAction> indexActions = new LinkedList<>();
        addDocumentToIndexActions(indexActions, IndexActionType.UPLOAD, new Document());
        client.index(new IndexBatch().actions(indexActions));
    }

    @Override
    protected void initializeClient() {
        client = builderSetup().indexName(INDEX_NAME).buildClient();
    }
}
