// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.models;

import com.azure.core.http.rest.PagedIterableBase;
import com.azure.search.SearchPagedResponse;

import com.azure.search.SearchIndexClient;
import com.azure.search.implementation.SerializationUtil;
import com.azure.search.SearchIndexClientTestBase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GeoPointTests extends SearchIndexClientTestBase {

    private static final String INDEX_NAME_HOTELS = "hotels";
    private static final String DATA_JSON_HOTELS = "HotelsDataArray.json";
    private static final String INDEX_JSON_GEO_POINTS = "GeoPointsIndexData.json";
    private static final String INDEX_NAME_GEO_POINTS = "geopoints";

    private SearchIndexClient client;

    private List<Map<String, Object>> uploadDocuments() throws Exception {
        Reader docsData = new InputStreamReader(
            getClass().getClassLoader().getResourceAsStream(GeoPointTests.DATA_JSON_HOTELS));

        ObjectMapper mapper = new ObjectMapper();
        SerializationUtil.configureMapper(mapper);
        List<Map<String, Object>> documents = mapper.readValue(docsData, List.class);
        client.uploadDocuments(documents);

        waitForIndexing();

        return documents;
    }

    @Test
    public void canDeserializeGeoPoint() throws Exception {
        createHotelIndex();
        client = getSearchIndexClientBuilder(INDEX_NAME_HOTELS).buildClient();

        uploadDocuments();
        SearchOptions searchOptions = new SearchOptions().setFilter("HotelId eq '1'");
        PagedIterableBase<SearchResult, SearchPagedResponse> results = client.search("Location", searchOptions, new RequestOptions());
        Assert.assertNotNull(results);

        GeoPoint geoPointObj = (GeoPoint) getSearchResults(results).get(0).get("Location");
        Assert.assertNotNull(geoPointObj);

        GeoPoint expected = GeoPoint.create(47.678581, -122.131577);
        Assert.assertEquals(expected, geoPointObj);
    }

    @Test
    public void canSerializeGeoPoint() {
        setupIndexFromJsonFile(INDEX_JSON_GEO_POINTS);
        client = getSearchIndexClientBuilder(INDEX_NAME_GEO_POINTS).buildClient();

        List<Map<String, Object>> docs = new ArrayList<>();

        Map<String, Object> doc = new LinkedHashMap<>();
        doc.put("Id", "1");
        doc.put("Name", "test");
        doc.put("Location", GeoPoint.create(1.0, 100.0));
        docs.add(doc);
        DocumentIndexResult indexResult = client.uploadDocuments(docs);

        Assert.assertNotNull(indexResult);
        Assert.assertTrue(indexResult.getResults().get(0).isSucceeded());
    }

    private List<Map<String, Object>> getSearchResults(PagedIterableBase<SearchResult, SearchPagedResponse> results) {
        Iterator<SearchPagedResponse> iterator = results.iterableByPage().iterator();
        List<Map<String, Object>> searchResults = new ArrayList<>();
        while (iterator.hasNext()) {
            SearchPagedResponse result = iterator.next();
            Assert.assertNotNull(result.getItems());
            result.getItems().forEach(item -> searchResults.add(item.getDocument()));
        }

        return searchResults;
    }
}
