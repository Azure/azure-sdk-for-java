// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.models;

import com.azure.search.documents.SearchClient;
import com.azure.search.documents.SearchDocument;
import com.azure.search.documents.SearchTestBase;
import com.azure.search.documents.TestHelpers;
import com.azure.search.documents.util.SearchPagedIterable;
import com.azure.search.documents.util.SearchPagedResponse;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.azure.search.documents.TestHelpers.waitForIndexing;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class GeoPointTests extends SearchTestBase {
    private static final String DATA_JSON_HOTELS = "HotelsDataArray.json";

    private SearchClient client;

    private void uploadDocuments() {
        InputStream docsData = Objects.requireNonNull(getClass().getClassLoader()
            .getResourceAsStream(GeoPointTests.DATA_JSON_HOTELS));

        List<Map<String, Object>> documents = TestHelpers.convertStreamToList(docsData);
        client.uploadDocuments(documents);

        waitForIndexing();
    }

    @Override
    protected void afterTest() {
        getSearchIndexClientBuilder().buildClient().deleteIndex(client.getIndexName());
    }

//    @Test
//    public void canDeserializeGeoPoint() {
//        client = getSearchClientBuilder(createHotelIndex()).buildClient();
//
//        uploadDocuments();
//        SearchOptions searchOptions = new SearchOptions().setFilter("HotelId eq '1'");
//        SearchPagedIterable results = client.search("Location",
//            searchOptions, Context.NONE);
//        assertNotNull(results);
//
//        PointGeometry expected = createPointGeometry(47.678581, -122.131577);
//        assertObjectEquals(expected, getSearchResults(results).get(0).get("Location"),
//            true, "properties");
//    }

//    @Test
//    public void canSerializeGeoPoint() {
//        SearchIndex index = new SearchIndex("geopoints")
//            .setFields(Arrays.asList(
//                new SearchField("Id", SearchFieldDataType.STRING)
//                    .setKey(true)
//                    .setFilterable(true)
//                    .setSortable(true),
//                new SearchField("Name", SearchFieldDataType.STRING)
//                    .setSearchable(true)
//                    .setFilterable(true)
//                    .setSortable(true),
//                new SearchField("Location", SearchFieldDataType.GEOGRAPHY_POINT)
//                    .setFilterable(true)
//                    .setSortable(true)
//            ));
//
//        client = getSearchClientBuilder(setupIndex(index)).buildClient();
//
//        List<Map<String, Object>> docs = new ArrayList<>();
//
//        Map<String, Object> doc = new LinkedHashMap<>();
//        doc.put("Id", "1");
//        doc.put("Name", "test");
//        doc.put("Location", createPointGeometry(1.0, 100.0));
//        docs.add(doc);
//        IndexDocumentsResult indexResult = client.uploadDocuments(docs);
//
//        assertNotNull(indexResult);
//        assertTrue(indexResult.getResults().get(0).isSucceeded());
//    }

    private List<Map<String, Object>> getSearchResults(SearchPagedIterable results) {
        Iterator<SearchPagedResponse> iterator = results.iterableByPage().iterator();
        List<Map<String, Object>> searchResults = new ArrayList<>();
        while (iterator.hasNext()) {
            SearchPagedResponse result = iterator.next();
            assertNotNull(result.getElements());
            result.getElements().forEach(item -> searchResults.add(item.getDocument(SearchDocument.class)));
        }

        return searchResults;
    }
}
