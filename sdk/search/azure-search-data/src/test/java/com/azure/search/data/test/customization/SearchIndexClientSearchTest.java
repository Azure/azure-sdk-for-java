// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.data.test.customization;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.search.data.SearchIndexClient;
import com.azure.search.data.common.DocumentResponseConversions;
import com.azure.search.data.common.SearchPagedResponse;
import com.azure.search.data.env.SearchIndexService;
import com.azure.search.data.generated.models.IndexAction;
import com.azure.search.data.generated.models.IndexActionType;
import com.azure.search.data.generated.models.IndexBatch;
import com.azure.search.data.generated.models.SearchParameters;
import com.azure.search.data.generated.models.SearchRequestOptions;
import com.azure.search.data.generated.models.SearchResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;

public class SearchIndexClientSearchTest extends SearchIndexClientTestBase {

    private SearchIndexClient client;
    private List<Map<String, Object>> hotels;

    private static final String INDEX_NAME = "hotels";
    private static final String HOTELS_DATA_JSON = "HotelsDataArray.json";
    private static final String SEARCH_SCORE_FIELD = "@search.score";
    private static final String MODEL_WITH_VALUE_TYPES_INDEX_JSON = "ModelWithValueTypesIndexData.json";
    private static final String MODEL_WITH_VALUE_TYPES_DOCS_JSON = "ModelWithValueTypesDocsData.json";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Override
    protected void beforeTest() {
        super.beforeTest();
        client = builderSetup().indexName(INDEX_NAME).buildClient();
        try {
            hotels = uploadDocuments(HOTELS_DATA_JSON);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<Map<String, Object>> uploadDocuments(String docsDataFile) throws IOException {
        Reader docsData = new InputStreamReader(
            getClass().getClassLoader().getResourceAsStream(docsDataFile));
        List<Map<String, Object>> docs = new ObjectMapper().readValue(docsData, List.class);
        List<IndexAction> indexActions = new LinkedList<>();

        docs.forEach(h -> {
            HashMap<String, Object> doc = new HashMap<String, Object>(h);
            indexActions.add(new IndexAction()
                .actionType(IndexActionType.UPLOAD)
                .additionalProperties(doc));
        });

        client.index(
            new IndexBatch().actions(indexActions));

        // Wait 2 secs to index finish
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return docs;
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
                Assert.assertEquals(1, item.score(), 0);
                Assert.assertNull(item.highlights());
                searchResults.add(item.additionalProperties());
            });
        }
        Assert.assertEquals(hotels.size(), searchResults.size());
        assertTrue(compareResults(searchResults, hotels));
    }


    /**
     * Verify that if searching and specifying fields, only those fields are returning
     */
    @Test
    public void canSearchWithSelectedFields() {
        // Ask JUST for the following two fields
        SearchParameters sp = new SearchParameters();
        sp.searchFields(new LinkedList<>(Arrays.asList("HotelName", "Category")));
        sp.select(new LinkedList<>(Arrays.asList("HotelName", "Rating", "Address/City", "Rooms/Type")));

        PagedIterable<SearchResult> results = client.search("fancy luxury secret", sp, new SearchRequestOptions());

        HashMap<String, Object> expectedHotel1 = new HashMap<>();
        expectedHotel1.put("HotelName", "Fancy Stay");
        expectedHotel1.put("Rating", 5);
        expectedHotel1.put("Address", null);
        expectedHotel1.put("Rooms", Arrays.asList());

        // This is the expected document when querying the document later (notice that only two fields are expected)
        HashMap<String, Object> expectedHotel2 = new HashMap<>();
        expectedHotel2.put("HotelName", "Secret Point Motel");
        expectedHotel2.put("Rating", 3);
        HashMap<String, Object> address = new LinkedHashMap<>();
        address.put("City", "New York");
        expectedHotel2.put("Address", address);
        HashMap<String, Object> rooms = new LinkedHashMap<>();
        rooms.put("Type", "Budget Room");
        HashMap<String, Object> rooms2 = new LinkedHashMap<>();
        rooms2.put("Type", "Budget Room");
        expectedHotel2.put("Rooms", Arrays.asList(rooms, rooms2));

        Iterator<PagedResponse<SearchResult>> iterator = results.iterableByPage().iterator();
        PagedResponse<SearchResult> result = iterator.next();
        assert (result.items().size() == 2);

        // From the result object, extract the two hotels, clean up (irrelevant fields) and change data structure
        // as a preparation to check equality
        Map<String, Object> hotel1 = extractAndTransformSingleResult(result.items().get(0));
        Map<String, Object> hotel2 = extractAndTransformSingleResult(result.items().get(1));

        Assert.assertEquals(expectedHotel1, hotel1);
        Assert.assertEquals(expectedHotel2, hotel2);
    }

    @Test
    public void testSearchWithoutOrderBySortsByScore() {
        Iterator<SearchResult> results = client.search("*", new SearchParameters().filter("Rating lt 4"), new SearchRequestOptions()).iterator();
        SearchResult firstResult =  results.next();
        SearchResult secondResult =  results.next();
        Assert.assertTrue(firstResult.score() <= secondResult.score());
    }

    @Test
    public void testOrderByProgressivelyBreaksTies() {
        List<String> orderByValues = new ArrayList<>();
        orderByValues.add("Rating desc");
        orderByValues.add("LastRenovationDate asc");

        String[] expectedResults = new String[]{"1", "9", "3", "4", "5", "10", "2", "6", "7", "8"};

        Stream<String> results = client.search("*", new SearchParameters().orderBy(orderByValues), new SearchRequestOptions()).stream().map(res -> res.additionalProperties().get("HotelId").toString());
        Assert.assertArrayEquals(results.toArray(), expectedResults);
    }

    @Test
    public void testCanFilter() {
        SearchParameters searchParameters = new SearchParameters()
            .filter("Rating gt 3 and LastRenovationDate gt 2000-01-01T00:00:00Z");
        PagedIterable<SearchResult> results = client.search("*", searchParameters, new SearchRequestOptions());
        Assert.assertNotNull(results);

        List<Map<String, Object>> searchResultsList = getSearchResults(results);
        Assert.assertEquals(2, searchResultsList.size());

        List hotelIds = searchResultsList.stream().map(r -> r.get("HotelId")).collect(Collectors.toList());
        Assert.assertTrue(Arrays.asList("1", "5").containsAll(hotelIds));
    }

    @Test
    public void testSearchThrowsWhenRequestIsMalformed() {
        thrown.expect(HttpResponseException.class);
        thrown.expectMessage("Invalid expression: Syntax error at position 7 in 'This is not a valid filter.'");

        SearchParameters invalidSearchParameters = new SearchParameters()
            .filter("This is not a valid filter.");

        PagedIterable<SearchResult> results = client.search("*", invalidSearchParameters, new SearchRequestOptions());
        results.iterableByPage().iterator().next();
    }

    @Test
    public void testCanFilterNonNullableType() throws IOException {
        /** TODO (Rabeea): This test is testing the case where a customer is using a model type with non-nullable (unboxed)
         primitive types. When we support user data-structured serialization, we need to use that in this test.
         **/
        if (!interceptorManager.isPlaybackMode()) {
            // In RECORDING mode (only), create a new index:
            SearchIndexService searchIndexService = new SearchIndexService(
                MODEL_WITH_VALUE_TYPES_INDEX_JSON, searchServiceName, apiKey);
            searchIndexService.initialize();
        }

        client.setIndexName("testindex");
        List<Map<String, Object>> docsList = uploadDocuments(MODEL_WITH_VALUE_TYPES_DOCS_JSON);
        List<Map<String, Object>> expectedDocsList = docsList.stream().filter(d -> !d.get("Key").equals("789")).collect(
            Collectors.toList());

        SearchParameters searchParameters = new SearchParameters()
            .filter("IntValue eq 0 or (Bucket/BucketName eq 'B' and Bucket/Count lt 10)");

        PagedIterable<SearchResult> results = client.search("*", searchParameters, new SearchRequestOptions());
        Assert.assertNotNull(results);

        List<Map<String, Object>> searchResultsList = getSearchResults(results);
        Assert.assertEquals(2, searchResultsList.size());

        searchResultsList.forEach(SearchIndexClientSearchTest::dropUnnecessaryFields);
        Assert.assertEquals(expectedDocsList, searchResultsList);
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

    private Map<String, Object> extractAndTransformSingleResult(SearchResult result) {
        return dropUnnecessaryFields(
            DocumentResponseConversions.convertLinkedHashMapToMap(
                (result.additionalProperties())));
    }

    /**
     * Drop fields that shouldn't be in the returned object
     *
     * @param map the map to drop items from
     * @return the new map
     */
    private static Map<String, Object> dropUnnecessaryFields(Map<String, Object> map) {
        map.remove(SEARCH_SCORE_FIELD);

        return map;
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
