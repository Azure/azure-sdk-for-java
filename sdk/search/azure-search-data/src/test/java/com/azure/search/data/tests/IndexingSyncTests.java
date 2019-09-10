// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.data.tests;

import com.azure.core.exception.HttpResponseException;
import com.azure.search.data.SearchIndexClient;
import com.azure.search.data.customization.Document;
import com.azure.search.data.customization.IndexingAction;
import com.azure.search.data.generated.models.*;
import com.azure.search.data.models.Hotel;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import java.text.ParseException;

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
        List<IndexingResult> results =  client.index(indexBatch).results();
        Assert.assertEquals(results.size(), indexBatch.actions().size());

        AssertSuccessfulIndexResult(results.get(0), "1", 201);
        AssertSuccessfulIndexResult(results.get(1), "randomId", 200);
        AssertFailedIndexResult(results.get(2), "nonExistingHotel", 404, "Document not found.");
        AssertSuccessfulIndexResult(results.get(3), "3", 201);
        AssertSuccessfulIndexResult(results.get(4), "2", 201);

        Hotel actualHotel1 = client.getDocument(hotel1.hotelId()).as(Hotel.class);
        Assert.assertEquals(hotel1, actualHotel1);

        Hotel actualHotel2 = client.getDocument(hotel2.hotelId()).as(Hotel.class);
        Assert.assertEquals(hotel2, actualHotel2);

        Hotel actualHotel3 = client.getDocument(hotel3.hotelId()).as(Hotel.class);
        Assert.assertEquals(hotel3, actualHotel3);
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
        List<IndexingResult> results =  client.index(indexBatch).results();
        Assert.assertEquals(results.size(), indexBatch.actions().size());

        AssertSuccessfulIndexResult(results.get(0), "1", 201);
        AssertSuccessfulIndexResult(results.get(1), "randomId", 200);
        AssertFailedIndexResult(results.get(2), "nonExistingHotel", 404, "Document not found.");
        AssertSuccessfulIndexResult(results.get(3), "3", 201);
        AssertSuccessfulIndexResult(results.get(4), "2", 201);

        Document actualHotel1 = client.getDocument(hotel1.get("HotelId").toString());
        Assert.assertEquals(hotel1, actualHotel1);

        Document actualHotel2 = client.getDocument(hotel2.get("HotelId").toString());
        Assert.assertEquals(hotel2, actualHotel2);

        Document actualHotel3 = client.getDocument(hotel3.get("HotelId").toString());
        Assert.assertEquals(hotel3, actualHotel3);
    }

    @Override
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
