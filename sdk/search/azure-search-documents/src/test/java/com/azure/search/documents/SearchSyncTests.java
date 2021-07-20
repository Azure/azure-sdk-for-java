// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.models.GeoPoint;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.models.SearchField;
import com.azure.search.documents.indexes.models.SearchFieldDataType;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.azure.search.documents.indexes.models.SynonymMap;
import com.azure.search.documents.models.FacetResult;
import com.azure.search.documents.models.QueryType;
import com.azure.search.documents.models.RangeFacetResult;
import com.azure.search.documents.models.ScoringParameter;
import com.azure.search.documents.models.SearchMode;
import com.azure.search.documents.models.SearchOptions;
import com.azure.search.documents.models.SearchResult;
import com.azure.search.documents.models.ValueFacetResult;
import com.azure.search.documents.test.environment.models.Bucket;
import com.azure.search.documents.test.environment.models.Hotel;
import com.azure.search.documents.test.environment.models.NonNullableModel;
import com.azure.search.documents.util.SearchPagedIterable;
import com.azure.search.documents.util.SearchPagedResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.HttpURLConnection;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.azure.search.documents.TestHelpers.assertHttpResponseException;
import static com.azure.search.documents.TestHelpers.assertMapEquals;
import static com.azure.search.documents.TestHelpers.assertObjectEquals;
import static com.azure.search.documents.TestHelpers.convertMapToValue;
import static com.azure.search.documents.TestHelpers.readJsonFileToList;
import static com.azure.search.documents.TestHelpers.setupSharedIndex;
import static com.azure.search.documents.TestHelpers.uploadDocuments;
import static com.azure.search.documents.TestHelpers.uploadDocumentsJson;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SearchSyncTests extends SearchTestBase {
    private final List<String> indexesToDelete = new ArrayList<>();
    private String synonymMapToDelete = "";

    private static final String INDEX_NAME = "azsearch-search-shared-instance";

    private static SearchIndexClient searchIndexClient;
    private SearchClient client;

    @BeforeAll
    public static void setupClass() {
        TestBase.setupClass();

        if (TEST_MODE == TestMode.PLAYBACK) {
            return;
        }

        searchIndexClient = setupSharedIndex(INDEX_NAME);
    }

    @Override
    protected void afterTest() {
        super.afterTest();

        SearchIndexClient serviceClient = getSearchIndexClientBuilder().buildClient();
        for (String index : indexesToDelete) {
            serviceClient.deleteIndex(index);
        }

        if (!CoreUtils.isNullOrEmpty(synonymMapToDelete)) {
            serviceClient.deleteSynonymMap(synonymMapToDelete);
            sleepIfRunningAgainstService(5000);
        }
    }

    @AfterAll
    protected static void cleanupClass() {
        if (TEST_MODE != TestMode.PLAYBACK) {
            searchIndexClient.deleteIndex(INDEX_NAME);
        }
    }

    private SearchClient setupClient(Supplier<String> indexSupplier) {
        String indexName = indexSupplier.get();
        indexesToDelete.add(indexName);

        return getSearchClientBuilder(indexName).buildClient();
    }

    @Test
    public void searchThrowsWhenRequestIsMalformed() {
        SearchOptions invalidSearchOptions = new SearchOptions().setFilter("This is not a valid filter.");

        assertHttpResponseException(
            () -> search("*", invalidSearchOptions),
            HttpURLConnection.HTTP_BAD_REQUEST,
            "Invalid expression: Syntax error at position 7 in 'This is not a valid filter.'");
    }

    @Test
    public void searchThrowsWhenSpecialCharInRegexIsUnescaped() {
        SearchOptions invalidSearchOptions = new SearchOptions().setQueryType(QueryType.FULL);

        assertHttpResponseException(
            () -> search("/.*/.*/", invalidSearchOptions),
            HttpURLConnection.HTTP_BAD_REQUEST,
            "Failed to parse query string at line 1, column 8.");
    }

    private void search(String searchText, SearchOptions searchOptions) {
        getSearchClientBuilder(INDEX_NAME).buildClient().search(searchText, searchOptions, Context.NONE)
            .iterableByPage()
            .iterator()
            .next();
    }

    @Test
    public void canSearchDynamicDocuments() {
        client = setupClient(this::createHotelIndex);

        List<Map<String, Object>> hotels = uploadDocumentsJson(client, HOTELS_DATA_JSON_WITHOUT_FR_DESCRIPTION);

        SearchPagedIterable searchResults = client.search("*");
        assertNotNull(searchResults);
        assertNull(searchResults.getTotalCount());
        assertNull(searchResults.getCoverage());
        assertNull(searchResults.getFacets());
        Iterator<SearchPagedResponse> iterator = searchResults.iterableByPage().iterator();

        List<Map<String, Object>> actualResults = new ArrayList<>(hotels.size());
        while (iterator.hasNext()) {
            SearchPagedResponse result = iterator.next();
            assertNotNull(result.getValue());

            result.getElements().forEach(item -> {
                assertEquals(1, item.getScore(), 0);
                assertNull(item.getHighlights());
                actualResults.add(item.getDocument(SearchDocument.class));
            });
        }
        assertEquals(hotels.size(), actualResults.size());
        actualResults.sort(Comparator.comparing(doc -> Integer.parseInt(doc.get("HotelId").toString())));
        assertTrue(compareResults(actualResults, hotels));
    }

    @Test
    public void canContinueSearch() {
        client = setupClient(this::createHotelIndex);

        // upload large documents batch
        List<Map<String, Object>> hotels = createHotelsList(100);
        uploadDocuments(client, hotels);

        SearchOptions searchOptions = new SearchOptions().setSelect("HotelId")
            .setOrderBy("HotelId asc");

        List<String> expectedHotelIds = hotels.stream().map(hotel -> (String) hotel.get("HotelId")).sorted()
            .collect(Collectors.toList());

        SearchPagedIterable results = client.search("*", searchOptions, Context.NONE);

        assertNotNull(results);

        Iterator<SearchPagedResponse> iterator = results.iterableByPage().iterator();

        SearchPagedResponse firstPage = iterator.next();
        assertEquals(50, firstPage.getValue().size());
        assertListEqualHotelIds(expectedHotelIds.subList(0, 50), firstPage.getValue());
        assertNotNull(firstPage.getContinuationToken());

        SearchPagedResponse secondPage = iterator.next();
        assertEquals(50, secondPage.getValue().size());
        assertListEqualHotelIds(expectedHotelIds.subList(50, 100), secondPage.getValue());
        assertNull(secondPage.getContinuationToken());
    }

    @Test
    public void canContinueSearchWithTop() {
        client = setupClient(this::createHotelIndex);

        // upload large documents batch
        List<Map<String, Object>> hotels = createHotelsList(3000);
        uploadDocuments(client, hotels);

        SearchOptions searchOptions = new SearchOptions()
            .setTop(2000)
            .setSelect("HotelId")
            .setOrderBy("HotelId asc");

        List<String> expectedHotelIds = hotels.stream().map(hotel -> (String) hotel.get("HotelId")).sorted()
            .collect(Collectors.toList());

        SearchPagedIterable results = client.search("*", searchOptions, Context.NONE);

        assertNotNull(results);

        Iterator<SearchPagedResponse> iterator = results.iterableByPage().iterator();

        SearchPagedResponse firstPage = iterator.next();
        assertEquals(1000, firstPage.getValue().size());
        assertListEqualHotelIds(expectedHotelIds.subList(0, 1000), firstPage.getValue());
        assertNotNull(firstPage.getContinuationToken());

        SearchPagedResponse secondPage = iterator.next();
        assertEquals(1000, secondPage.getValue().size());
        assertListEqualHotelIds(expectedHotelIds.subList(1000, 2000), secondPage.getValue());
        assertNull(secondPage.getContinuationToken());
    }

    @Test
    public void canSearchStaticallyTypedDocuments() {
        client = setupClient(this::createHotelIndex);

        List<Map<String, Object>> hotels = uploadDocumentsJson(client, HOTELS_DATA_JSON_WITHOUT_FR_DESCRIPTION);

        SearchPagedIterable results = client.search("*", new SearchOptions(), Context.NONE);
        assertNotNull(results);

        assertNull(results.getTotalCount());
        assertNull(results.getCoverage());
        assertNull(results.getFacets());
        Iterator<SearchPagedResponse> iterator = results.iterableByPage().iterator();

        List<Hotel> actualResults = new ArrayList<>(hotels.size());
        while (iterator.hasNext()) {
            SearchPagedResponse result = iterator.next();
            assertNotNull(result.getValue());

            result.getElements().forEach(item -> {
                assertEquals(1, item.getScore(), 0);
                assertNull(item.getHighlights());
                Hotel hotel = item.getDocument(Hotel.class);
                actualResults.add(hotel);
            });
        }

        List<Hotel> hotelsList = hotels.stream()
            .map(hotel -> convertMapToValue(hotel, Hotel.class))
            .collect(Collectors.toList());
        assertEquals(hotelsList.size(), actualResults.size());
        actualResults.sort(Comparator.comparing(doc -> Integer.parseInt(doc.hotelId())));
        for (int i = 0; i < hotelsList.size(); i++) {
            assertObjectEquals(hotelsList.get(i), actualResults.get(i), true, "properties");
        }
    }

    @SuppressWarnings("UseOfObsoleteDateTimeApi")
    @Test
    public void canRoundTripNonNullableValueTypes() {
        client = setupClient(this::createIndexWithNonNullableTypes);

        Date startEpoch = Date.from(Instant.ofEpochMilli(1275346800000L));
        NonNullableModel doc1 = new NonNullableModel()
            .key("123")
            .count(3)
            .isEnabled(true)
            .rating(5)
            .ratio(3.14)
            .startDate(new Date(startEpoch.getTime()))
            .endDate(new Date(startEpoch.getTime()))
            .topLevelBucket(new Bucket().bucketName("A").count(12))
            .buckets(new Bucket[]{new Bucket().bucketName("B").count(20), new Bucket().bucketName("C").count(7)});

        NonNullableModel doc2 = new NonNullableModel().key("456").buckets(new Bucket[]{});

        uploadDocuments(client, Arrays.asList(doc1, doc2));

        SearchPagedIterable results = client.search("*", new SearchOptions(), Context.NONE);
        assertNotNull(results);
        Iterator<SearchPagedResponse> iterator = results.iterableByPage().iterator();
        assertTrue(iterator.hasNext());

        SearchPagedResponse result = iterator.next();
        assertEquals(2, result.getValue().size());
        assertObjectEquals(doc1, result.getValue().get(0).getDocument(NonNullableModel.class), true);
        assertObjectEquals(doc2, result.getValue().get(1).getDocument(NonNullableModel.class), true);
    }

    @SuppressWarnings("UseOfObsoleteDateTimeApi")
    @Test
    public void canSearchWithDateInStaticModel() {
        client = getSearchClientBuilder(INDEX_NAME).buildClient();

        OffsetDateTime expected = OffsetDateTime.parse("2010-06-27T00:00:00Z");

        SearchPagedIterable results = client.search("Fancy", new SearchOptions(), Context.NONE);
        assertNotNull(results);
        Iterator<SearchPagedResponse> iterator = results.iterableByPage().iterator();
        assertTrue(iterator.hasNext());

        SearchPagedResponse result = iterator.next();
        assertEquals(1, result.getValue().size());
        Date actual = result.getValue().get(0).getDocument(Hotel.class).lastRenovationDate();
        long epochMilli = expected.toInstant().toEpochMilli();
        assertEquals(new Date(epochMilli), actual);
    }

    @Test
    public void canSearchWithSelectedFields() {
        client = getSearchClientBuilder(INDEX_NAME).buildClient();

        // Ask JUST for the following two fields
        SearchOptions sp = new SearchOptions();
        sp.setSearchFields("HotelName", "Category");
        sp.setSelect("HotelName", "Rating", "Address/City", "Rooms/Type");

        SearchPagedIterable results = client.search("fancy luxury secret", sp, Context.NONE);

        HashMap<String, Object> expectedHotel1 = new HashMap<>();
        expectedHotel1.put("HotelName", "Fancy Stay");
        expectedHotel1.put("Rating", 5);
        expectedHotel1.put("Address", null);
        expectedHotel1.put("Rooms", Collections.emptyList());

        // This is the expected document when querying the document later (notice that only two fields are expected)
        HashMap<String, Object> expectedHotel2 = new HashMap<>();
        expectedHotel2.put("HotelName", "Secret Point Motel");
        expectedHotel2.put("Rating", 4);
        HashMap<String, Object> address = new HashMap<>();
        address.put("City", "New York");
        expectedHotel2.put("Address", address);
        HashMap<String, Object> rooms = new HashMap<>();
        rooms.put("Type", "Budget Room");
        HashMap<String, Object> rooms2 = new HashMap<>();
        rooms2.put("Type", "Budget Room");
        expectedHotel2.put("Rooms", Arrays.asList(rooms, rooms2));

        Iterator<SearchPagedResponse> iterator = results.iterableByPage().iterator();
        SearchPagedResponse result = iterator.next();
        assertEquals(2, result.getValue().size());

        // From the result object, extract the two hotels, clean up (irrelevant fields) and change data structure
        // as a preparation to check equality
        Map<String, Object> hotel1 = extractAndTransformSingleResult(result.getValue().get(0));
        Map<String, Object> hotel2 = extractAndTransformSingleResult(result.getValue().get(1));

        assertMapEquals(expectedHotel1, hotel1, true);
        assertMapEquals(expectedHotel2, hotel2, true);
    }

    @Test
    public void canUseTopAndSkipForClientSidePaging() {
        client = getSearchClientBuilder(INDEX_NAME).buildClient();

        SearchOptions parameters = new SearchOptions().setTop(3).setSkip(0).setOrderBy("HotelId");

        SearchPagedIterable results = client.search("*", parameters, Context.NONE);
        assertKeySequenceEqual(results, Arrays.asList("1", "10", "2"));

        parameters.setSkip(3);
        results = client.search("*", parameters, Context.NONE);
        assertKeySequenceEqual(results, Arrays.asList("3", "4", "5"));
    }

    @Test
    public void searchWithoutOrderBySortsByScore() {
        client = getSearchClientBuilder(INDEX_NAME).buildClient();

        Iterator<SearchResult> results = client
            .search("*", new SearchOptions().setFilter("Rating lt 4"), Context.NONE).iterator();
        SearchResult firstResult = results.next();
        SearchResult secondResult = results.next();
        assertTrue(firstResult.getScore() <= secondResult.getScore());
    }

    @Test
    public void orderByProgressivelyBreaksTies() {
        client = getSearchClientBuilder(INDEX_NAME).buildClient();

        String[] expectedResults = new String[]{"1", "9", "3", "4", "5", "10", "2", "6", "7", "8"};

        Stream<String> results = client
            .search("*", new SearchOptions().setOrderBy("Rating desc", "LastRenovationDate asc", "HotelId"),
                Context.NONE).stream()
            .map(this::getSearchResultId);
        assertArrayEquals(results.toArray(), expectedResults);
    }

    @Test
    public void canFilter() {
        client = getSearchClientBuilder(INDEX_NAME).buildClient();

        SearchOptions searchOptions = new SearchOptions()
            .setFilter("Rating gt 3 and LastRenovationDate gt 2000-01-01T00:00:00Z");
        SearchPagedIterable results = client.search("*", searchOptions, Context.NONE);
        assertNotNull(results);

        List<Map<String, Object>> searchResultsList = getSearchResults(results);
        assertEquals(2, searchResultsList.size());
        assertEquals("1", searchResultsList.get(0).get("HotelId").toString());
        assertEquals("5", searchResultsList.get(1).get("HotelId").toString());
    }

    @Test
    public void canSearchWithRangeFacets() {
        client = getSearchClientBuilder(INDEX_NAME).buildClient();

        List<Map<String, Object>> hotels = readJsonFileToList(HOTELS_DATA_JSON);

        SearchPagedIterable results = client.search("*", getSearchOptionsForRangeFacets(),
            Context.NONE);

        assertNotNull(results.getFacets());
        List<RangeFacetResult<String>> baseRateFacets = getRangeFacetsForField(results.getFacets(),
            "Rooms/BaseRate", 4);
        List<RangeFacetResult<String>> lastRenovationDateFacets = getRangeFacetsForField(
            results.getFacets(), "LastRenovationDate", 2);
        assertRangeFacets(baseRateFacets, lastRenovationDateFacets);

        for (SearchPagedResponse result : results.iterableByPage()) {
            assertContainHotelIds(hotels, result.getValue());
        }
    }

    @Test
    public void canSearchWithValueFacets() {
        client = getSearchClientBuilder(INDEX_NAME).buildClient();

        List<Map<String, Object>> hotels = readJsonFileToList(HOTELS_DATA_JSON);

        SearchPagedIterable results = client.search("*", getSearchOptionsForValueFacets(),
            Context.NONE);

        Map<String, List<FacetResult>> facets = results.getFacets();
        assertNotNull(facets);
        for (SearchPagedResponse result : results.iterableByPage()) {
            assertContainHotelIds(hotels, result.getValue());

            assertValueFacetsEqual(
                getValueFacetsForField(facets, "Rating", 2),
                new ArrayList<>(Arrays.asList(
                    new ValueFacetResult<>(1L, 5),
                    new ValueFacetResult<>(4L, 4))));

            assertValueFacetsEqual(
                getValueFacetsForField(facets, "SmokingAllowed", 2),
                new ArrayList<>(Arrays.asList(
                    new ValueFacetResult<>(4L, false),
                    new ValueFacetResult<>(2L, true))));

            assertValueFacetsEqual(
                getValueFacetsForField(facets, "Category", 3),
                new ArrayList<>(Arrays.asList(
                    new ValueFacetResult<>(5L, "Budget"),
                    new ValueFacetResult<>(1L, "Boutique"),
                    new ValueFacetResult<>(1L, "Luxury"))));

            assertValueFacetsEqual(getValueFacetsForField(facets, "LastRenovationDate", 6),
                new ArrayList<>(Arrays.asList(new ValueFacetResult<>(1L, OffsetDateTime.parse("1970-01-01T00:00:00Z").format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)),
                    new ValueFacetResult<>(1L, OffsetDateTime.parse("1982-01-01T00:00:00Z").format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)),
                    new ValueFacetResult<>(2L, OffsetDateTime.parse("1995-01-01T00:00:00Z").format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)),
                    new ValueFacetResult<>(1L, OffsetDateTime.parse("1999-01-01T00:00:00Z").format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)),
                    new ValueFacetResult<>(1L, OffsetDateTime.parse("2010-01-01T00:00:00Z").format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)),
                    new ValueFacetResult<>(1L, OffsetDateTime.parse("2012-01-01T00:00:00Z").format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)))));

            assertValueFacetsEqual(
                getValueFacetsForField(facets, "Rooms/BaseRate", 4),
                new ArrayList<>(Arrays.asList(
                    new ValueFacetResult<>(1L, 2.44),
                    new ValueFacetResult<>(1L, 7.69),
                    new ValueFacetResult<>(1L, 8.09),
                    new ValueFacetResult<>(1L, 9.69))));

            assertValueFacetsEqual(
                getValueFacetsForField(facets, "Tags", 10),
                new ArrayList<>(Arrays.asList(
                    new ValueFacetResult<>(1L, "24-hour front desk service"),
                    new ValueFacetResult<>(1L, "air conditioning"),
                    new ValueFacetResult<>(4L, "budget"),
                    new ValueFacetResult<>(1L, "coffee in lobby"),
                    new ValueFacetResult<>(2L, "concierge"),
                    new ValueFacetResult<>(1L, "motel"),
                    new ValueFacetResult<>(2L, "pool"),
                    new ValueFacetResult<>(1L, "restaurant"),
                    new ValueFacetResult<>(1L, "view"),
                    new ValueFacetResult<>(4L, "wifi"))));
        }
    }

    @Test
    public void canSearchWithLuceneSyntax() {
        client = getSearchClientBuilder(INDEX_NAME).buildClient();

        Map<String, Object> expectedResult = new HashMap<>();
        expectedResult.put("HotelName", "Roach Motel");
        expectedResult.put("Rating", 1);

        SearchOptions searchOptions = new SearchOptions().setQueryType(QueryType.FULL).setSelect("HotelName", "Rating");
        SearchPagedIterable results = client.search("HotelName:roch~", searchOptions,
            Context.NONE);

        assertNotNull(results);
        List<Map<String, Object>> searchResultsList = getSearchResults(results);
        assertEquals(1, searchResultsList.size());
        assertEquals(expectedResult, searchResultsList.get(0));
    }

    @Test
    public void canFilterNonNullableType() {
        client = setupClient(this::createIndexWithValueTypes);

        List<Map<String, Object>> docsList = createDocsListWithValueTypes();
        uploadDocuments(client, docsList);

        List<Map<String, Object>> expectedDocsList =
            docsList.stream()
                .filter(d -> !d.get("Key").equals("789"))
                .collect(Collectors.toList());

        SearchOptions searchOptions = new SearchOptions()
            .setFilter("IntValue eq 0 or (Bucket/BucketName eq 'B' and Bucket/Count lt 10)");

        SearchPagedIterable results = client.search("*", searchOptions, Context.NONE);
        assertNotNull(results);

        List<Map<String, Object>> searchResultsList = getSearchResults(results);
        assertEquals(2, searchResultsList.size());
        for (int i = 0; i < searchResultsList.size(); i++) {
            assertObjectEquals(expectedDocsList.get(i), searchResultsList.get(i), true);
        }
    }

    @Test
    public void canSearchWithSearchModeAll() {
        client = getSearchClientBuilder(INDEX_NAME).buildClient();

        List<Map<String, Object>> response = getSearchResults(client
            .search("Cheapest hotel", new SearchOptions().setQueryType(QueryType.SIMPLE).setSearchMode(SearchMode.ALL),
                Context.NONE));
        assertEquals(1, response.size());
        assertEquals("2", response.get(0).get("HotelId"));
    }

    @Test
    public void defaultSearchModeIsAny() {
        client = getSearchClientBuilder(INDEX_NAME).buildClient();

        List<Map<String, Object>> response = getSearchResults(client.search("Cheapest hotel",
            new SearchOptions().setOrderBy("HotelId"), Context.NONE));
        assertEquals(7, response.size());
        assertEquals(
            Arrays.asList("1", "10", "2", "3", "4", "5", "9"),
            response.stream().map(res -> res.get("HotelId").toString()).collect(Collectors.toList()));
    }

    @Test
    public void canGetResultCountInSearch() {
        client = getSearchClientBuilder(INDEX_NAME).buildClient();
        List<Map<String, Object>> hotels = readJsonFileToList(HOTELS_DATA_JSON);

        SearchPagedIterable results = client.search("*", new SearchOptions().setIncludeTotalCount(true),
            Context.NONE);
        assertNotNull(results);
        assertEquals(hotels.size(), results.getTotalCount().intValue());

        Iterator<SearchPagedResponse> iterator = results.iterableByPage().iterator();

        assertNotNull(iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    public void canSearchWithRegex() {
        client = getSearchClientBuilder(INDEX_NAME).buildClient();

        SearchOptions searchOptions = new SearchOptions()
            .setQueryType(QueryType.FULL)
            .setSelect("HotelName", "Rating");

        SearchPagedIterable results = client.search("HotelName:/.*oach.*\\/?/", searchOptions,
            Context.NONE);
        assertNotNull(results);

        List<Map<String, Object>> resultsList = getSearchResults(results);

        Map<String, Object> expectedHotel = new HashMap<>();
        expectedHotel.put("HotelName", "Roach Motel");
        expectedHotel.put("Rating", 1);

        assertEquals(1, resultsList.size());
        assertEquals(resultsList.get(0), expectedHotel);
    }

    @Test
    public void canSearchWithEscapedSpecialCharsInRegex() {
        client = getSearchClientBuilder(INDEX_NAME).buildClient();

        SearchOptions searchOptions = new SearchOptions().setQueryType(QueryType.FULL);

        SearchPagedIterable results = client.search("\\+\\-\\&\\|\\!\\(\\)\\{\\}\\[\\]\\^\\~\\*\\?\\:", searchOptions,
            Context.NONE);
        assertNotNull(results);

        List<Map<String, Object>> resultsList = getSearchResults(results);
        assertEquals(0, resultsList.size());
    }

    @Test
    public void searchWithScoringProfileBoostsScore() {
        client = setupClient(this::createHotelIndex);

        uploadDocumentsJson(client, HOTELS_DATA_JSON);
        SearchOptions searchOptions = new SearchOptions()
            .setScoringProfile("nearest")
            .setScoringParameters(new ScoringParameter("myloc", new GeoPoint(-122.0, 49.0)))
            .setFilter("Rating eq 5 or Rating eq 1");

        List<Map<String, Object>> response = getSearchResults(client.search("hotel", searchOptions, Context.NONE));
        assertEquals(2, response.size());
        assertEquals("2", response.get(0).get("HotelId").toString());
        assertEquals("1", response.get(1).get("HotelId").toString());
    }

    @Test
    public void searchWithScoringProfileEscaper() {
        client = getSearchClientBuilder(INDEX_NAME).buildClient();

        SearchOptions searchOptions = new SearchOptions()
            .setScoringProfile("text")
            .setScoringParameters(new ScoringParameter("mytag", Arrays.asList("concierge", "Hello, O''Brien")))
            .setFilter("Rating eq 5 or Rating eq 1");

        List<Map<String, Object>> response = getSearchResults(client.search("hotel",
            searchOptions, Context.NONE));
        assertEquals(2, response.size());
        assertEquals(
            Arrays.asList("1", "2"),
            response.stream().map(res -> res.get("HotelId").toString()).collect(Collectors.toList()));
    }

    @Test
    public void searchWithScoringParametersEmpty() {
        client = getSearchClientBuilder(INDEX_NAME).buildClient();

        SearchOptions searchOptions = new SearchOptions()
            .setScoringProfile("text")
            .setScoringParameters(new ScoringParameter("mytag", Arrays.asList("", "concierge")))
            .setFilter("Rating eq 5 or Rating eq 1");

        List<Map<String, Object>> response = getSearchResults(client.search("hotel",
            searchOptions, Context.NONE));
        assertEquals(2, response.size());
        assertEquals(
            Arrays.asList("1", "2"),
            response.stream().map(res -> res.get("HotelId").toString()).collect(Collectors.toList()));
    }

    @Test
    public void canSearchWithMinimumCoverage() {
        client = getSearchClientBuilder(INDEX_NAME).buildClient();

        SearchPagedIterable results = client.search("*", new SearchOptions().setMinimumCoverage(50.0),
            Context.NONE);
        assertNotNull(results);

        assertEquals(100.0, results.getCoverage(), 0);
    }

    @Test
    public void canUseHitHighlighting() {
        client = getSearchClientBuilder(INDEX_NAME).buildClient();

        //arrange
        String description = "Description";
        String category = "Category";

        SearchOptions sp = new SearchOptions();
        sp.setFilter("Rating eq 5");
        sp.setHighlightPreTag("<b>");
        sp.setHighlightPostTag("</b>");
        sp.setHighlightFields(category, description);

        //act
        SearchPagedIterable results = client.search("luxury hotel", sp, Context.NONE);

        //sanity
        assertNotNull(results);
        Iterator<SearchPagedResponse> iterator = results.iterableByPage().iterator();
        SearchPagedResponse result = iterator.next();
        List<SearchResult> documents = result.getValue();

        // sanity
        assertEquals(1, documents.size());
        Map<String, List<String>> highlights = documents.get(0).getHighlights();
        assertNotNull(highlights);
        assertEquals(2, highlights.keySet().size());
        assertTrue(highlights.containsKey(description));
        assertTrue(highlights.containsKey(category));

        String categoryHighlight = highlights.get(category).get(0);

        //asserts
        assertEquals("<b>Luxury</b>", categoryHighlight);

        // Typed as IEnumerable so we get the right overload of Equals below.
        List<String> expectedDescriptionHighlights =
            Arrays.asList(
                "Best <b>hotel</b> in town if you like <b>luxury</b> <b>hotels</b>.",
                "We highly recommend this <b>hotel</b>."
            );

        assertEquals(expectedDescriptionHighlights, highlights.get(description));
    }

    @Test
    public void canSearchWithSynonyms() {
        client = setupClient(this::createHotelIndex);

        uploadDocumentsJson(client, HOTELS_DATA_JSON);

        String fieldName = "HotelName";
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder().buildClient();

        // Create a new SynonymMap
        synonymMapToDelete = searchIndexClient.createSynonymMap(new SynonymMap(
            testResourceNamer.randomName("names", 32))
            .setSynonyms("luxury,fancy")).getName();

        // Attach index field to SynonymMap
        SearchIndex hotelsIndex = searchIndexClient.getIndex(client.getIndexName());
        hotelsIndex.getFields().stream()
            .filter(f -> fieldName.equals(f.getName()))
            .findFirst().get().setSynonymMapNames(synonymMapToDelete);

        // Update the index with the SynonymMap
        searchIndexClient.createOrUpdateIndex(hotelsIndex);

        sleepIfRunningAgainstService(10000);

        SearchOptions searchOptions = new SearchOptions()
            .setQueryType(QueryType.FULL)
            .setSearchFields(fieldName)
            .setSelect("HotelName", "Rating");

        SearchPagedIterable results = client.search("luxury", searchOptions, Context.NONE);
        assertNotNull(results);

        List<Map<String, Object>> response = getSearchResults(results);
        assertEquals(1, response.size());
        assertEquals("Fancy Stay", response.get(0).get("HotelName"));
        assertEquals(5, response.get(0).get("Rating"));
    }

    private List<Map<String, Object>> getSearchResults(SearchPagedIterable results) {
        Iterator<SearchPagedResponse> resultsIterator = results.iterableByPage().iterator();

        List<Map<String, Object>> searchResults = new ArrayList<>();
        while (resultsIterator.hasNext()) {
            SearchPagedResponse result = resultsIterator.next();
            assertNotNull(result.getValue());
            result.getElements().forEach(item -> searchResults.add(item.getDocument(SearchDocument.class)));
        }

        return searchResults;
    }

    private Map<String, Object> extractAndTransformSingleResult(SearchResult result) {
        return convertHashMapToMap((result.getDocument(SearchDocument.class)));
    }

    /**
     * Convert a HashMap object to Map object
     *
     * @param mapObject object to convert
     * @return {@link Map}{@code <}{@link String}{@code ,}{@link Object}{@code >}
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> convertHashMapToMap(Object mapObject) {
        // This SuppressWarnings is for the checkstyle it is used because api return type can
        // be anything and therefore is an Object in our case we know and we use it only when
        // the return type is LinkedHashMap. The object is converted into HashMap (which LinkedHashMap
        // extends)
        HashMap<String, Object> map = (HashMap<String, Object>) mapObject;

        Set<Map.Entry<String, Object>> entries = map.entrySet();

        Map<String, Object> convertedMap = new HashMap<>();

        for (Map.Entry<String, Object> entry : entries) {
            Object value = entry.getValue();

            if (value instanceof HashMap) {
                value = convertHashMapToMap(entry.getValue());
            }
            if (value instanceof ArrayList) {
                value = convertArray((ArrayList<Object>) value);

            }

            convertedMap.put(entry.getKey(), value);
        }

        return convertedMap;
    }

    /**
     * Convert Array Object elements
     *
     * @param array which elements will be converted
     * @return {@link ArrayList}{@code <}{@link Object}{@code >}
     */
    private static ArrayList<Object> convertArray(ArrayList<Object> array) {
        ArrayList<Object> convertedArray = new ArrayList<>();
        for (Object arrayValue : array) {
            if (arrayValue instanceof HashMap) {
                convertedArray.add(convertHashMapToMap(arrayValue));
            } else {
                convertedArray.add(arrayValue);
            }
        }
        return convertedArray;
    }

    private void assertKeySequenceEqual(SearchPagedIterable results, List<String> expectedKeys) {
        assertNotNull(results);

        List<String> actualKeys = results.stream().filter(doc -> doc.getDocument(SearchDocument.class)
            .containsKey("HotelId"))
            .map(doc -> (String) doc.getDocument(SearchDocument.class).get("HotelId")).collect(Collectors.toList());

        assertEquals(expectedKeys, actualKeys);
    }

    List<Map<String, Object>> createHotelsList(int count) {
        List<Map<String, Object>> documents = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            Map<String, Object> doc = new HashMap<>();

            doc.put("HotelId", Integer.toString(i));
            doc.put("HotelName", "Hotel" + i);
            doc.put("Description", "Desc" + i);
            doc.put("Description_fr", "Desc_fr" + i);
            doc.put("Category", "Catg" + i);
            doc.put("Tags", Collections.singletonList("tag" + i));
            doc.put("ParkingIncluded", false);
            doc.put("SmokingAllowed", false);
            doc.put("LastRenovationDate", OffsetDateTime.parse("2010-06-27T00:00:00Z"));
            doc.put("Rating", i);

            documents.add(doc);
        }
        return documents;
    }

    boolean compareResults(List<Map<String, Object>> searchResults, List<Map<String, Object>> hotels) {
        Iterator<Map<String, Object>> searchIterator = searchResults.iterator();
        Iterator<Map<String, Object>> hotelsIterator = hotels.iterator();
        while (searchIterator.hasNext() && hotelsIterator.hasNext()) {
            Map<String, Object> result = searchIterator.next();
            Map<String, Object> hotel = hotelsIterator.next();

            assertMapEquals(hotel, result, true, "properties");
        }

        return true;
    }

    <T> void assertRangeFacets(List<RangeFacetResult<T>> baseRateFacets, List<RangeFacetResult<T>> lastRenovationDateFacets) {
        assertNull(baseRateFacets.get(0).getFrom());
        assertEquals(5.0, baseRateFacets.get(0).getTo());
        assertEquals(5.0, baseRateFacets.get(1).getFrom());
        assertEquals(8.0, baseRateFacets.get(1).getTo());
        assertEquals(8.0, baseRateFacets.get(2).getFrom());
        assertEquals(10.0, baseRateFacets.get(2).getTo());
        assertEquals(10.0, baseRateFacets.get(3).getFrom());
        assertNull(baseRateFacets.get(3).getTo());

        assertEquals(1, baseRateFacets.get(0).getCount().intValue());
        assertEquals(1, baseRateFacets.get(1).getCount().intValue());
        assertEquals(1, baseRateFacets.get(2).getCount().intValue());
        assertEquals(0, baseRateFacets.get(3).getCount().intValue());

        assertNull(lastRenovationDateFacets.get(0).getFrom());
        assertEquals("2000-01-01T00:00:00.000+0000", lastRenovationDateFacets.get(0).getTo());
        assertEquals("2000-01-01T00:00:00.000+0000", lastRenovationDateFacets.get(1).getFrom());
        assertNull(lastRenovationDateFacets.get(1).getTo());

        assertEquals(5, lastRenovationDateFacets.get(0).getCount().intValue());
        assertEquals(2, lastRenovationDateFacets.get(1).getCount().intValue());
    }

    <T> List<RangeFacetResult<T>> getRangeFacetsForField(
        Map<String, List<FacetResult>> facets, String expectedField, int expectedCount) {
        List<FacetResult> facetCollection = getFacetsForField(facets, expectedField, expectedCount);
        return facetCollection.stream().map(RangeFacetResult<T>::new).collect(Collectors.toList());
    }

    <T> List<ValueFacetResult<T>> getValueFacetsForField(
        Map<String, List<FacetResult>> facets, String expectedField, int expectedCount) {
        List<FacetResult> facetCollection = getFacetsForField(facets, expectedField, expectedCount);
        return facetCollection.stream().map(ValueFacetResult<T>::new)
            .collect(Collectors.toList());
    }

    private List<FacetResult> getFacetsForField(
        Map<String, List<FacetResult>> facets, String expectedField, int expectedCount) {
        assertTrue(facets.containsKey(expectedField));
        List<FacetResult> results = facets.get(expectedField);
        assertEquals(expectedCount, results.size());
        return results;
    }

    void assertContainHotelIds(List<Map<String, Object>> expected, List<SearchResult> actual) {
        assertNotNull(actual);
        Set<String> actualKeys = actual.stream().filter(item -> item.getDocument(SearchDocument.class)
            .containsKey("HotelId")).map(item -> (String) item.getDocument(SearchDocument.class)
            .get("HotelId")).collect(Collectors.toSet());
        Set<String> expectedKeys = expected.stream().filter(item -> item.containsKey("HotelId"))
            .map(item -> (String) item.get("HotelId")).collect(Collectors.toSet());
        assertEquals(expectedKeys, actualKeys);
    }

    <T> void assertValueFacetsEqual(List<ValueFacetResult<T>> actualFacets, ArrayList<ValueFacetResult<T>> expectedFacets) {
        assertEquals(expectedFacets.size(), actualFacets.size());
        for (int i = 0; i < actualFacets.size(); i++) {
            assertEquals(expectedFacets.get(i).getCount(), actualFacets.get(i).getCount());
            assertEquals(expectedFacets.get(i).getValue(), actualFacets.get(i).getValue());
        }
    }

    String getSearchResultId(SearchResult searchResult) {
        return searchResult.getDocument(SearchDocument.class).get("HotelId").toString();
    }

    SearchOptions getSearchOptionsForRangeFacets() {
        return new SearchOptions().setFacets("Rooms/BaseRate,values:5|8|10",
            "LastRenovationDate,values:2000-01-01T00:00:00Z");
    }

    SearchOptions getSearchOptionsForValueFacets() {
        return new SearchOptions().setFacets("Rating,count:2,sort:-value",
            "SmokingAllowed,sort:count",
            "Category",
            "LastRenovationDate,interval:year",
            "Rooms/BaseRate,sort:value",
            "Tags,sort:value");
    }

    void assertListEqualHotelIds(List<String> expected, List<SearchResult> actual) {
        assertNotNull(actual);
        List<String> actualKeys = actual.stream().filter(item -> item.getDocument(SearchDocument.class)
            .containsKey("HotelId")).map(item -> (String) item.getDocument(SearchDocument.class)
            .get("HotelId")).collect(Collectors.toList());
        assertEquals(expected, actualKeys);
    }

    String createIndexWithNonNullableTypes() {
        SearchIndex index = new SearchIndex("non-nullable-index")
            .setFields(Arrays.asList(
                new SearchField("Key", SearchFieldDataType.STRING)
                    .setHidden(false)
                    .setKey(true),
                new SearchField("Rating", SearchFieldDataType.INT32)
                    .setHidden(false),
                new SearchField("Count", SearchFieldDataType.INT64)
                    .setHidden(false),
                new SearchField("IsEnabled", SearchFieldDataType.BOOLEAN)
                    .setHidden(false),
                new SearchField("Ratio", SearchFieldDataType.DOUBLE)
                    .setHidden(false),
                new SearchField("StartDate", SearchFieldDataType.DATE_TIME_OFFSET)
                    .setHidden(false),
                new SearchField("EndDate", SearchFieldDataType.DATE_TIME_OFFSET)
                    .setHidden(false),
                new SearchField("TopLevelBucket", SearchFieldDataType.COMPLEX)
                    .setFields(Arrays.asList(
                        new SearchField("BucketName", SearchFieldDataType.STRING)
                            .setFilterable(true),
                        new SearchField("Count", SearchFieldDataType.INT32)
                            .setFilterable(true))),
                new SearchField("Buckets", SearchFieldDataType.collection(SearchFieldDataType.COMPLEX))
                    .setFields(Arrays.asList(
                        new SearchField("BucketName", SearchFieldDataType.STRING)
                            .setFilterable(true),
                        new SearchField("Count", SearchFieldDataType.INT32)
                            .setFilterable(true)))));

        setupIndex(index);

        return index.getName();
    }

    String createIndexWithValueTypes() {
        SearchIndex index = new SearchIndex("testindex")
            .setFields(Arrays.asList(
                new SearchField("Key", SearchFieldDataType.STRING)
                    .setKey(true)
                    .setSearchable(true),
                new SearchField("IntValue", SearchFieldDataType.INT32)
                    .setFilterable(true),
                new SearchField("Bucket", SearchFieldDataType.COMPLEX)
                    .setFields(Arrays.asList(
                        new SearchField("BucketName", SearchFieldDataType.STRING)
                            .setFilterable(true),
                        new SearchField("Count", SearchFieldDataType.INT32)
                            .setFilterable(true)
                    ))
                )
            );

        setupIndex(index);

        return index.getName();
    }

    List<Map<String, Object>> createDocsListWithValueTypes() {
        Map<String, Object> element1 = new HashMap<>();
        element1.put("Key", "123");
        element1.put("IntValue", 0);

        Map<String, Object> subElement1 = new HashMap<>();
        subElement1.put("BucketName", "A");
        subElement1.put("Count", 3);
        element1.put("Bucket", subElement1);

        Map<String, Object> element2 = new HashMap<>();
        element2.put("Key", "456");
        element2.put("IntValue", 7);

        Map<String, Object> subElement2 = new HashMap<>();
        subElement2.put("BucketName", "B");
        subElement2.put("Count", 5);
        element2.put("Bucket", subElement2);

        Map<String, Object> element3 = new HashMap<>();
        element3.put("Key", "789");
        element3.put("IntValue", 1);

        Map<String, Object> subElement3 = new HashMap<>();
        subElement3.put("BucketName", "B");
        subElement3.put("Count", 99);
        element3.put("Bucket", subElement3);

        return Arrays.asList(element1, element2, element3);
    }
}
