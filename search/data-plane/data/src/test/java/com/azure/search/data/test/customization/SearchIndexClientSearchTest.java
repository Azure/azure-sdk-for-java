// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.data.test.customization;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.search.data.SearchIndexClient;
import com.azure.search.data.common.SearchPagedResponse;
import com.azure.search.data.generated.models.IndexAction;
import com.azure.search.data.generated.models.IndexActionType;
import com.azure.search.data.generated.models.IndexBatch;
import com.azure.search.data.generated.models.SearchParameters;
import com.azure.search.data.generated.models.SearchRequestOptions;
import com.azure.search.data.generated.models.SearchResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;

public class SearchIndexClientSearchTest extends SearchIndexClientTestBase {

    private SearchIndexClient client;
    private List<Map<String, Object>> hotels;

    private static final String INDEX_NAME = "hotels";
    private static final String HOTELS_DATA_JSON = "HotelsDataArray.json";

    @Override
    protected void beforeTest() {
        super.beforeTest();
        client = builderSetup().indexName(INDEX_NAME).buildClient();
        try {
            uploadDocuments();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void uploadDocuments() throws IOException {
        Reader docsData = new InputStreamReader(
            getClass().getClassLoader().getResourceAsStream(HOTELS_DATA_JSON));
        hotels = new ObjectMapper().readValue(docsData, List.class);
        List<IndexAction> indexActions = new LinkedList<>();

        hotels.forEach(h -> {
            HashMap<String, Object> hotel = new HashMap<String, Object>(h);
            indexActions.add(new IndexAction()
                .actionType(IndexActionType.UPLOAD)
                .additionalProperties(hotel));
        });

        client.index(
            new IndexBatch().actions(indexActions));
    }

    @Test
    public void testCanSyncSearchDynamicDocuments() {
        PagedIterable<SearchResult> results = client.search("*", new SearchParameters(), new SearchRequestOptions());

        Assert.assertNotNull(results);

        Iterator<PagedResponse<SearchResult>> iterator = results.iterableByPage().iterator();
        List<Map<String, Object>> searchResults = new ArrayList<>();
        while (iterator.hasNext()) {
            SearchPagedResponse result = (SearchPagedResponse) iterator.next();
            Assert.assertNull(result.count());
            Assert.assertNull(result.coverage());
            Assert.assertNull(result.facets());
            Assert.assertNotNull(result.items());

            result.items().forEach(item -> {
                Assert.assertEquals(1, ((SearchResult) item).score(), 0);
                Assert.assertNull(((SearchResult) item).highlights());
                searchResults.add(((SearchResult) item).additionalProperties());
            });
        }
        Assert.assertEquals(hotels.size(), searchResults.size());
        assertTrue(compareResults(searchResults, hotels));
    }

    private boolean compareResults(List<Map<String, Object>> searchResults, List<Map<String, Object>> hotels) {
        Iterator<Map<String, Object>> searchIterator = searchResults.iterator();
        Iterator<Map<String, Object>> hotelsIterator = hotels.iterator();
        while (searchIterator.hasNext() && hotelsIterator.hasNext()) {
            Map<String, Object> result = searchIterator.next();
            Map<String, Object> hotel = hotelsIterator.next();


            // do not compare location object
            // TODO (Nava) - remove once geo location issue is resolved
            result.remove("Location");
            hotel.remove("Location");
            /*if (!hotel.entrySet().stream().allMatch(e -> checkEquals(e, result))) {
                return false;
            }*/
            assertTrue(hotel.entrySet().stream().allMatch(e -> checkEquals(e, result)));
        }

        return true;
    }

    private boolean checkEquals(Map.Entry<String, Object> hotel, Map<String, Object> result) {
        if (hotel.getValue() != null) {
            return hotel.getValue().equals(result.get(hotel.getKey()));
        }
        return true;
    }
}
