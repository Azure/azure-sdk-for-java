// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.models;

import com.azure.core.util.Context;
import com.azure.search.documents.SearchIndexClient;
import com.azure.search.documents.SearchTestBase;
import com.azure.search.documents.implementation.SerializationUtil;
import com.azure.search.documents.util.SearchPagedIterable;
import com.azure.search.documents.util.SearchPagedResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.azure.search.documents.TestHelpers.waitForIndexing;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GeoPointTests extends SearchTestBase {
    private static final String DATA_JSON_HOTELS = "HotelsDataArray.json";

    private SearchIndexClient client;

    private void uploadDocuments() throws Exception {
        Reader docsData = new InputStreamReader(Objects.requireNonNull(getClass().getClassLoader()
            .getResourceAsStream(GeoPointTests.DATA_JSON_HOTELS)));

        ObjectMapper mapper = new ObjectMapper();
        SerializationUtil.configureMapper(mapper);
        List<Map<String, Object>> documents = mapper.readValue(docsData,
            new TypeReference<List<Map<String, Object>>>() {
            });
        client.uploadDocuments(documents);

        waitForIndexing();
    }

    @Override
    protected void afterTest() {
        getSearchServiceClientBuilder().buildClient().deleteIndex(client.getIndexName());
    }

    @Test
    public void canDeserializeGeoPoint() throws Exception {
        client = getSearchIndexClientBuilder(createHotelIndex()).buildClient();

        uploadDocuments();
        SearchOptions searchOptions = new SearchOptions().setFilter("HotelId eq '1'");
        SearchPagedIterable results = client.search("Location",
            searchOptions, new RequestOptions(), Context.NONE);
        assertNotNull(results);

        GeoPoint geoPointObj = (GeoPoint) getSearchResults(results).get(0).get("Location");
        assertNotNull(geoPointObj);

        GeoPoint expected = GeoPoint.create(47.678581, -122.131577);
        assertEquals(expected, geoPointObj);
    }

    @Test
    public void canSerializeGeoPoint() {
        Index index = new Index()
            .setName("geopoints")
            .setFields(Arrays.asList(
                new Field()
                    .setName("Id")
                    .setType(DataType.EDM_STRING)
                    .setKey(true)
                    .setFilterable(true)
                    .setSortable(true),
                new Field()
                    .setName("Name")
                    .setType(DataType.EDM_STRING)
                    .setSearchable(true)
                    .setFilterable(true)
                    .setSortable(true),
                new Field()
                    .setName("Location")
                    .setType(DataType.EDM_GEOGRAPHY_POINT)
                    .setFilterable(true)
                    .setSortable(true)
            ));

        client = getSearchIndexClientBuilder(setupIndex(index)).buildClient();

        List<Map<String, Object>> docs = new ArrayList<>();

        Map<String, Object> doc = new LinkedHashMap<>();
        doc.put("Id", "1");
        doc.put("Name", "test");
        doc.put("Location", GeoPoint.create(1.0, 100.0));
        docs.add(doc);
        IndexDocumentsResult indexResult = client.uploadDocuments(docs);

        assertNotNull(indexResult);
        assertTrue(indexResult.getResults().get(0).isSucceeded());
    }

    private List<Map<String, Object>> getSearchResults(SearchPagedIterable results) {
        Iterator<SearchPagedResponse> iterator = results.iterableByPage().iterator();
        List<Map<String, Object>> searchResults = new ArrayList<>();
        while (iterator.hasNext()) {
            SearchPagedResponse result = iterator.next();
            assertNotNull(result.getElements());
            result.getElements().forEach(item -> searchResults.add(item.getDocument()));
        }

        return searchResults;
    }
}
