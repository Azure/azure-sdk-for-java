// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.data.tests;

import com.azure.core.exception.HttpResponseException;
import com.azure.search.data.SearchIndexClient;
import com.azure.search.data.common.jsonwrapper.JsonWrapper;
import com.azure.search.data.common.jsonwrapper.api.JsonApi;
import com.azure.search.data.common.jsonwrapper.jacksonwrapper.JacksonDeserializer;
import com.azure.search.data.customization.Document;

import com.azure.search.data.generated.models.*;

import com.azure.search.data.models.Hotel;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

import java.util.*;

import java.text.ParseException;
import java.util.stream.Collectors;

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
        JsonApi jsonApi = JsonWrapper.newInstance(JacksonDeserializer.class);
        jsonApi.configureTimezone();

        Hotel hotel1 = prepareStaticallyTypedHotel("1");
        Hotel hotel2 = prepareStaticallyTypedHotel("2");
        Hotel hotel3 = prepareStaticallyTypedHotel("3");
        Hotel nonExistingHotel = prepareStaticallyTypedHotel("nonExistingHotel");
        Hotel randomHotel = prepareStaticallyTypedHotel("randomId");

        IndexAction uploadAction = new IndexAction()
            .actionType(IndexActionType.UPLOAD)
            .additionalProperties(jsonApi.convertObjectToType(hotel1, Map.class));

        IndexAction deleteAction = new IndexAction()
            .actionType(IndexActionType.DELETE)
            .additionalProperties(jsonApi.convertObjectToType(randomHotel, Map.class));
        IndexAction mergeNonExistingAction = new IndexAction()
            .actionType(IndexActionType.MERGE)
            .additionalProperties(jsonApi.convertObjectToType(nonExistingHotel, Map.class));
        IndexAction mergeOrUploadAction = new IndexAction()
            .actionType(IndexActionType.MERGE_OR_UPLOAD)
            .additionalProperties(jsonApi.convertObjectToType(hotel3, Map.class));
        IndexAction uploadAction2 = new IndexAction()
            .actionType(IndexActionType.UPLOAD)
            .additionalProperties(jsonApi.convertObjectToType(hotel2, Map.class));

        IndexBatch indexBatch = new IndexBatch().actions(Arrays.asList(
            uploadAction,
            deleteAction,
            mergeNonExistingAction,
            mergeOrUploadAction,
            uploadAction2
        ));
        List<IndexingResult> results =  client.index(indexBatch).results();
        Assert.assertEquals(results.size(), indexBatch.actions().size());

        assertSuccessfulIndexResult(results.get(0), "1", 201);
        assertSuccessfulIndexResult(results.get(1), "randomId", 200);
        assertFailedIndexResult(results.get(2), "nonExistingHotel", 404, "Document not found.");
        assertSuccessfulIndexResult(results.get(3), "3", 201);
        assertSuccessfulIndexResult(results.get(4), "2", 201);

        Hotel actualHotel1 = client.getDocument(hotel1.hotelId()).as(Hotel.class);
        Assert.assertEquals(hotel1, actualHotel1);

        Hotel actualHotel2 = client.getDocument(hotel2.hotelId()).as(Hotel.class);
        Assert.assertEquals(hotel2, actualHotel2);

        Hotel actualHotel3 = client.getDocument(hotel3.hotelId()).as(Hotel.class);
        Assert.assertEquals(hotel3, actualHotel3);
    }

    @Override
    public void canIndexDynamicDocuments() {
        JsonApi jsonApi = JsonWrapper.newInstance(JacksonDeserializer.class);
        jsonApi.configureTimezone();

        Document hotel1 = prepareDynamicallyTypedHotel("1");
        Document hotel2 = prepareDynamicallyTypedHotel("2");
        Document hotel3 = prepareDynamicallyTypedHotel("3");
        Document nonExistingHotel = prepareDynamicallyTypedHotel("nonExistingHotel");
        Document randomHotel = prepareDynamicallyTypedHotel("randomId");

        IndexAction uploadAction = new IndexAction()
            .actionType(IndexActionType.UPLOAD)
            .additionalProperties(hotel1);

        IndexAction deleteAction = new IndexAction()
            .actionType(IndexActionType.DELETE)
            .additionalProperties(randomHotel);

        IndexAction mergeNonExistingAction = new IndexAction()
            .actionType(IndexActionType.MERGE)
            .additionalProperties(nonExistingHotel);

        IndexAction mergeOrUploadAction = new IndexAction()
            .actionType(IndexActionType.MERGE_OR_UPLOAD)
            .additionalProperties(hotel3);

        IndexAction uploadAction2 = new IndexAction()
            .actionType(IndexActionType.UPLOAD)
            .additionalProperties(hotel2);

        IndexBatch indexBatch = new IndexBatch().actions(Arrays.asList(
            uploadAction,
            deleteAction,
            mergeNonExistingAction,
            mergeOrUploadAction,
            uploadAction2
        ));

        List<IndexingResult> results =  client.index(indexBatch).results();
        Assert.assertEquals(results.size(), indexBatch.actions().size());

        assertSuccessfulIndexResult(results.get(0), "1", 201);
        assertSuccessfulIndexResult(results.get(1), "randomId", 200);
        assertFailedIndexResult(results.get(2), "nonExistingHotel", 404, "Document not found.");
        assertSuccessfulIndexResult(results.get(3), "3", 201);
        assertSuccessfulIndexResult(results.get(4), "2", 201);

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
    public void canRoundtripBoundaryValues() throws Exception {
        JsonApi jsonApi = JsonWrapper.newInstance(JacksonDeserializer.class);
        jsonApi.configureTimezone();

        List<Hotel> boundaryConditionDocs = getBoundaryValues();

        List<IndexAction> actions = boundaryConditionDocs.stream()
            .map(h -> new IndexAction()
                .actionType(IndexActionType.UPLOAD)
                .additionalProperties((Map<String, Object>) jsonApi.convertObjectToType(h, Map.class)))
            .collect(Collectors.toList());
        IndexBatch batch = new IndexBatch()
            .actions(actions);

        client.index(batch);

        // Wait 2 secs to allow index request to finish
        Thread.sleep(2000);

        for (Hotel expected : boundaryConditionDocs) {
            Document doc = client.getDocument(expected.hotelId());
            Hotel actual = doc.as(Hotel.class);
            Assert.assertEquals(expected, actual);
        }
    }

    @Override
    protected void initializeClient() {
        client = builderSetup().indexName(INDEX_NAME).buildClient();
    }
}
