// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.data.customization.models;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.search.data.common.SearchPagedResponse;

import com.azure.search.data.customization.SearchIndexClient;
import com.azure.search.data.implementation.SerializationUtil;
import com.azure.search.data.customization.SearchIndexClientTestBase;
import com.azure.search.data.generated.models.DocumentIndexResult;
import com.azure.search.data.generated.models.SearchParameters;
import com.azure.search.data.generated.models.SearchRequestOptions;
import com.azure.search.data.generated.models.SearchResult;
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

    @Override
    protected void beforeTest() {
        super.beforeTest();
    }

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
        client = getClientBuilder(INDEX_NAME_HOTELS).buildClient();

        uploadDocuments();
        SearchParameters searchParameters = new SearchParameters().filter("HotelId eq '1'");
        PagedIterable<SearchResult> results = client.search("Location", searchParameters, new SearchRequestOptions());
        Assert.assertNotNull(results);

        GeoPoint geoPointObj = (GeoPoint) getSearchResults(results).get(0).get("Location");
        Assert.assertNotNull(geoPointObj);

        GeoPoint expected = GeoPoint.create(47.678581, -122.131577);
        Assert.assertEquals(expected, geoPointObj);
    }

    @Test
    public void canSerializeGeoPoint() {
        setupIndexFromJsonFile(INDEX_JSON_GEO_POINTS);
        client = getClientBuilder(INDEX_NAME_GEO_POINTS).buildClient();

        List<Map<String, Object>> docs = new ArrayList<>();

        Map<String, Object> doc = new LinkedHashMap<>();
        doc.put("Id", "1");
        doc.put("Name", "test");
        doc.put("Location", GeoPoint.create(1.0, 100.0));
        docs.add(doc);
        DocumentIndexResult indexResult = client.uploadDocuments(docs);

        Assert.assertNotNull(indexResult);
        Assert.assertTrue(indexResult.results().get(0).succeeded());
    }

    private List<Map<String, Object>> getSearchResults(PagedIterable<SearchResult> results) {
        Iterator<PagedResponse<SearchResult>> iterator = results.iterableByPage().iterator();
        List<Map<String, Object>> searchResults = new ArrayList<>();
        while (iterator.hasNext()) {
            SearchPagedResponse result = (SearchPagedResponse) iterator.next();
            Assert.assertNotNull(result.items());
            result.items().forEach(item -> searchResults.add(item.additionalProperties()));
        }

        return searchResults;
    }
}
