// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.models.GeoPoint;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.test.annotation.LiveOnly;
import com.azure.core.util.Context;
import com.azure.search.documents.implementation.util.SearchPagedResponseAccessHelper;
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
import com.azure.search.documents.util.SearchPagedFlux;
import com.azure.search.documents.util.SearchPagedIterable;
import com.azure.search.documents.util.SearchPagedResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.HttpURLConnection;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.azure.search.documents.TestHelpers.assertMapEquals;
import static com.azure.search.documents.TestHelpers.assertObjectEquals;
import static com.azure.search.documents.TestHelpers.convertMapToValue;
import static com.azure.search.documents.TestHelpers.readJsonFileToList;
import static com.azure.search.documents.TestHelpers.setupSharedIndex;
import static com.azure.search.documents.TestHelpers.uploadDocuments;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SearchTests extends SearchTestBase {
    private final List<String> indexesToDelete = new ArrayList<>();

    private static final String HOTEL_INDEX_NAME = "azsearch-search-shared-hotel-instance";
    private static final String LARGE_INDEX_NAME = "azsearch-search-shared-large-instance";
    private static final String SYNONYM_INDEX_NAME = "azsearch-search-shared-synonym-instance";
    private static final String SYNONYM_NAME = "azsearch-search-shared-synonym-instance";

    private static SearchIndexClient searchIndexClient;

    @BeforeAll
    public static void setupClass() {
        TestBase.setupClass();

        if (TEST_MODE == TestMode.PLAYBACK) {
            return;
        }

        searchIndexClient = setupSharedIndex(HOTEL_INDEX_NAME, HOTELS_TESTS_INDEX_DATA_JSON, HOTELS_DATA_JSON);
        setupSharedIndex(SYNONYM_INDEX_NAME, HOTELS_TESTS_INDEX_DATA_JSON, HOTELS_DATA_JSON);
        setupSharedIndex(LARGE_INDEX_NAME, HOTELS_TESTS_INDEX_DATA_JSON, null);

        uploadDocuments(searchIndexClient.getSearchClient(LARGE_INDEX_NAME), createHotelsList());

        searchIndexClient.createSynonymMap(new SynonymMap(SYNONYM_NAME).setSynonyms("luxury,fancy"));

        // Attach index field to SynonymMap
        SearchIndex hotelsIndex = searchIndexClient.getIndex(SYNONYM_INDEX_NAME);
        hotelsIndex.getFields().stream()
            .filter(f -> "HotelName".equals(f.getName()))
            .findFirst().orElseThrow(NoSuchElementException::new)
            .setSynonymMapNames(SYNONYM_NAME);
        searchIndexClient.createOrUpdateIndex(hotelsIndex);
    }

    @Override
    protected void afterTest() {
        super.afterTest();

        SearchIndexClient serviceClient = getSearchIndexClientBuilder(true).buildClient();
        for (String index : indexesToDelete) {
            serviceClient.deleteIndex(index);
        }
    }

    @AfterAll
    protected static void cleanupClass() {
        if (TEST_MODE != TestMode.PLAYBACK) {
            searchIndexClient.deleteIndex(HOTEL_INDEX_NAME);
            searchIndexClient.deleteIndex(LARGE_INDEX_NAME);
            searchIndexClient.deleteIndex(SYNONYM_INDEX_NAME);
            searchIndexClient.deleteSynonymMap(SYNONYM_NAME);

            // Sleep to ensure the synonym map delete finishes.
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private SearchClient getClient(String indexName) {
        return getSearchClientBuilder(indexName, true).buildClient();
    }

    private SearchAsyncClient getAsyncClient(String indexName) {
        return getSearchClientBuilder(indexName, false).buildAsyncClient();
    }

    @Test
    public void searchThrowsWhenRequestIsMalformedSync() {
        badSearchSync("*", new SearchOptions().setFilter("This is not a valid filter."));
    }

    @Test
    public void searchThrowsWhenRequestIsMalformedAsync() {
        badSearchAsync("*", new SearchOptions().setFilter("This is not a valid filter."));
    }

    @Test
    public void searchThrowsWhenSpecialCharInRegexIsUnescapedSync() {
        badSearchSync("/.*/.*/", new SearchOptions().setQueryType(QueryType.FULL));
    }

    @Test
    public void searchThrowsWhenSpecialCharInRegexIsUnescapedAsync() {
        badSearchAsync("/.*/.*/", new SearchOptions().setQueryType(QueryType.FULL));
    }

    private void badSearchSync(String searchText, SearchOptions searchOptions) {
        HttpResponseException ex = assertThrows(HttpResponseException.class,
            () -> getSearchClientBuilder(HOTEL_INDEX_NAME, true).buildClient().search(searchText, searchOptions,
                    Context.NONE)
                .iterableByPage()
                .iterator()
                .next());

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, ex.getResponse().getStatusCode());
    }

    private void badSearchAsync(String searchText, SearchOptions searchOptions) {
        StepVerifier.create(getSearchClientBuilder(HOTEL_INDEX_NAME, false).buildAsyncClient()
                .search(searchText, searchOptions)
                .byPage())
            .thenRequest(1)
            .verifyErrorSatisfies(throwable -> {
                HttpResponseException ex = assertInstanceOf(HttpResponseException.class, throwable);
                assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, ex.getResponse().getStatusCode());
            });
    }

    @Test
    public void canSearchDynamicDocumentsSync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);

        List<Map<String, Object>> hotels = readJsonFileToList(HOTELS_DATA_JSON);
        Map<String, Map<String, Object>> expectedHotels = hotels.stream()
            .collect(Collectors.toMap(h -> h.get("HotelId").toString(), Function.identity()));

        for (SearchPagedResponse response : client.search("*").iterableByPage()) {
            assertNull(SearchPagedResponseAccessHelper.getCount(response));
            assertNull(SearchPagedResponseAccessHelper.getCoverage(response));
            assertNull(SearchPagedResponseAccessHelper.getFacets(response));

            response.getElements().forEach(item -> {
                assertEquals(1, item.getScore(), 0);
                assertNull(item.getHighlights());

                SearchDocument actual = item.getDocument(SearchDocument.class);
                Map<String, Object> expected = expectedHotels.remove(actual.get("HotelId").toString());

                assertNotNull(expected);
                assertMapEquals(expected, actual, true, "properties");
            });
        }

        assertEquals(0, expectedHotels.size());
    }

    @Test
    public void canSearchDynamicDocumentsAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        List<Map<String, Object>> hotels = readJsonFileToList(HOTELS_DATA_JSON);
        Map<String, Map<String, Object>> expectedHotels = hotels.stream()
            .collect(Collectors.toMap(h -> h.get("HotelId").toString(), Function.identity()));

        StepVerifier.create(asyncClient.search("*").byPage())
            .thenConsumeWhile(response -> {
                assertNull(SearchPagedResponseAccessHelper.getCount(response));
                assertNull(SearchPagedResponseAccessHelper.getCoverage(response));
                assertNull(SearchPagedResponseAccessHelper.getFacets(response));

                response.getElements().forEach(item -> {
                    assertEquals(1, item.getScore(), 0);
                    assertNull(item.getHighlights());

                    SearchDocument actual = item.getDocument(SearchDocument.class);
                    Map<String, Object> expected = expectedHotels.remove(actual.get("HotelId").toString());

                    assertNotNull(expected);
                    assertMapEquals(expected, actual, true, "properties");
                });

                return true;
            })
            .verifyComplete();

        assertEquals(0, expectedHotels.size());
    }

    @Test
    public void canContinueSearchSync() {
        SearchClient client = getClient(LARGE_INDEX_NAME);

        // upload large documents batch
        List<Map<String, Object>> hotels = createHotelsList();

        SearchOptions searchOptions = new SearchOptions().setSelect("HotelId")
            .setOrderBy("HotelId asc");

        List<String> expectedHotelIds = hotels.stream().map(hotel -> (String) hotel.get("HotelId")).sorted()
            .collect(Collectors.toList());

        SearchPagedIterable results = client.search("*", searchOptions, Context.NONE);

        // By default, if top isn't specified in the SearchOptions each page will contain 50 results.
        AtomicInteger total = new AtomicInteger();
        results.iterableByPage().forEach(page -> {
            assertEquals(50, page.getValue().size());
            assertListEqualHotelIds(expectedHotelIds.subList(total.get(), total.addAndGet(50)), page.getValue());
            if (total.get() != 3000) {
                assertNotNull(page.getContinuationToken());
            } else {
                assertNull(page.getContinuationToken());
            }
        });
    }

    @Test
    public void canContinueSearchAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(LARGE_INDEX_NAME);

        // upload large documents batch
        List<Map<String, Object>> hotels = createHotelsList();

        SearchOptions searchOptions = new SearchOptions().setSelect("HotelId")
            .setOrderBy("HotelId asc");

        List<String> expectedHotelIds = hotels.stream().map(hotel -> (String) hotel.get("HotelId")).sorted()
            .collect(Collectors.toList());

        // By default, if top isn't specified in the SearchOptions each page will contain 50 results.
        StepVerifier.create(asyncClient.search("*", searchOptions).byPage().collectList())
            .assertNext(pages -> {
                AtomicInteger total = new AtomicInteger();
                pages.forEach(page -> {
                    assertEquals(50, page.getValue().size());
                    assertListEqualHotelIds(expectedHotelIds.subList(total.get(), total.addAndGet(50)),
                        page.getValue());
                    if (total.get() != 3000) {
                        assertNotNull(page.getContinuationToken());
                    } else {
                        assertNull(page.getContinuationToken());
                    }
                });
            })
            .verifyComplete();
    }

    @Test
    public void canContinueSearchWithTopSync() {
        SearchClient client = getClient(LARGE_INDEX_NAME);

        // upload large documents batch
        List<Map<String, Object>> hotels = createHotelsList();

        SearchOptions searchOptions = new SearchOptions()
            .setTop(2000)
            .setSelect("HotelId")
            .setOrderBy("HotelId asc");

        List<String> expectedHotelIds = hotels.stream().map(hotel -> (String) hotel.get("HotelId")).sorted()
            .collect(Collectors.toList());

        SearchPagedIterable results = client.search("*", searchOptions, Context.NONE);

        assertNotNull(results);

        Iterator<SearchPagedResponse> iterator = results.iterableByPage().iterator();

        try (SearchPagedResponse firstPage = iterator.next()) {
            assertEquals(1000, firstPage.getValue().size());
            assertListEqualHotelIds(expectedHotelIds.subList(0, 1000), firstPage.getValue());
            assertNotNull(firstPage.getContinuationToken());
        }

        try (SearchPagedResponse secondPage = iterator.next()) {
            assertEquals(1000, secondPage.getValue().size());
            assertListEqualHotelIds(expectedHotelIds.subList(1000, 2000), secondPage.getValue());
            assertNull(secondPage.getContinuationToken());
        }
    }

    @Test
    public void canContinueSearchWithTopAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(LARGE_INDEX_NAME);

        // upload large documents batch
        List<Map<String, Object>> hotels = createHotelsList();

        SearchOptions searchOptions = new SearchOptions()
            .setTop(2000)
            .setSelect("HotelId")
            .setOrderBy("HotelId asc");

        List<String> expectedHotelIds = hotels.stream().map(hotel -> (String) hotel.get("HotelId")).sorted()
            .collect(Collectors.toList());

        StepVerifier.create(asyncClient.search("*", searchOptions).byPage())
            .assertNext(response -> {
                assertEquals(1000, response.getValue().size());
                assertListEqualHotelIds(expectedHotelIds.subList(0, 1000), response.getValue());
                assertNotNull(response.getContinuationToken());
            })
            .assertNext(response -> {
                assertEquals(1000, response.getValue().size());
                assertListEqualHotelIds(expectedHotelIds.subList(1000, 2000), response.getValue());
                assertNull(response.getContinuationToken());
            })
            .verifyComplete();
    }

    @Test
    public void canSearchStaticallyTypedDocumentsSync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);

        List<Map<String, Object>> hotels = readJsonFileToList(HOTELS_DATA_JSON);
        Map<String, Hotel> expectedHotels = hotels.stream()
            .map(hotel -> convertMapToValue(hotel, Hotel.class))
            .collect(Collectors.toMap(Hotel::hotelId, Function.identity()));

        for (SearchPagedResponse response : client.search("*", new SearchOptions(), Context.NONE).iterableByPage()) {
            assertNull(SearchPagedResponseAccessHelper.getCount(response));
            assertNull(SearchPagedResponseAccessHelper.getCoverage(response));
            assertNull(SearchPagedResponseAccessHelper.getFacets(response));

            response.getElements().forEach(sr -> {
                assertEquals(1, sr.getScore(), 0);
                assertNull(sr.getHighlights());
                Hotel actual = sr.getDocument(Hotel.class);

                Hotel expected = expectedHotels.remove(actual.hotelId());
                assertNotNull(expected);
                assertObjectEquals(expected, actual, true, "properties");
            });
        }

        assertEquals(0, expectedHotels.size());
    }

    @Test
    public void canSearchStaticallyTypedDocumentsAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        List<Map<String, Object>> hotels = readJsonFileToList(HOTELS_DATA_JSON);
        Map<String, Hotel> expectedHotels = hotels.stream()
            .map(hotel -> convertMapToValue(hotel, Hotel.class))
            .collect(Collectors.toMap(Hotel::hotelId, Function.identity()));

        StepVerifier.create(asyncClient.search("*", new SearchOptions()).byPage())
            .thenConsumeWhile(response -> {
                assertNull(SearchPagedResponseAccessHelper.getCount(response));
                assertNull(SearchPagedResponseAccessHelper.getCoverage(response));
                assertNull(SearchPagedResponseAccessHelper.getFacets(response));

                response.getElements().forEach(sr -> {
                    assertEquals(1, sr.getScore(), 0);
                    assertNull(sr.getHighlights());
                    Hotel actual = sr.getDocument(Hotel.class);

                    Hotel expected = expectedHotels.remove(actual.hotelId());
                    assertNotNull(expected);
                    assertObjectEquals(expected, actual, true, "properties");
                });

                return true;
            })
            .verifyComplete();

        assertEquals(0, expectedHotels.size());
    }

    @SuppressWarnings("UseOfObsoleteDateTimeApi")
    @Test
    public void canRoundTripNonNullableValueTypesSyncAndAsync() {
        String indexName = createIndexWithNonNullableTypes();
        indexesToDelete.add(indexName);
        SearchClient client = getSearchClientBuilder(indexName, true).buildClient();

        Date startEpoch = Date.from(Instant.ofEpochMilli(1275346800000L));
        NonNullableModel doc1 = new NonNullableModel()
            .key("123")
            .count(3)
            .isEnabled(true)
            .rating(5)
            .ratio(3.25)
            .startDate(new Date(startEpoch.getTime()))
            .endDate(new Date(startEpoch.getTime()))
            .topLevelBucket(new Bucket().bucketName("A").count(12))
            .buckets(new Bucket[]{new Bucket().bucketName("B").count(20), new Bucket().bucketName("C").count(7)});

        NonNullableModel doc2 = new NonNullableModel().key("456").buckets(new Bucket[]{});

        Map<String, NonNullableModel> expectedDocs = new HashMap<>();
        expectedDocs.put(doc1.key(), doc1);
        expectedDocs.put(doc2.key(), doc2);

        uploadDocuments(client, Arrays.asList(doc1, doc2));

        SearchPagedIterable results = client.search("*", new SearchOptions(), Context.NONE);
        Iterator<SearchPagedResponse> iterator = results.iterableByPage().iterator();

        SearchPagedResponse result = iterator.next();
        Map<String, NonNullableModel> actualDocs = result.getValue().stream()
            .map(sr -> sr.getDocument(NonNullableModel.class))
            .collect(Collectors.toMap(NonNullableModel::key, Function.identity()));

        compareMaps(expectedDocs, actualDocs, (expected, actual) -> assertObjectEquals(expected, actual, true));

        SearchAsyncClient asyncClient = getSearchClientBuilder(indexName, false).buildAsyncClient();
        NonNullableModel doc1Async = new NonNullableModel()
            .key("123async")
            .count(3)
            .isEnabled(true)
            .rating(5)
            .ratio(3.25)
            .startDate(new Date(startEpoch.getTime()))
            .endDate(new Date(startEpoch.getTime()))
            .topLevelBucket(new Bucket().bucketName("A").count(12))
            .buckets(new Bucket[]{new Bucket().bucketName("B").count(20), new Bucket().bucketName("C").count(7)});

        NonNullableModel doc2Async = new NonNullableModel().key("456async").buckets(new Bucket[]{});

        Map<String, NonNullableModel> expectedDocsAsync = new HashMap<>();
        expectedDocsAsync.put(doc1Async.key(), doc1Async);
        expectedDocsAsync.put(doc2Async.key(), doc2Async);

        uploadDocuments(asyncClient, Arrays.asList(doc1Async, doc2Async));

        StepVerifier.create(asyncClient.search("*", new SearchOptions()).byPage())
            .assertNext(response -> {
                Map<String, NonNullableModel> actualDocsAsync = response.getValue().stream()
                    .map(sr -> sr.getDocument(NonNullableModel.class))
                    .filter(model -> model.key().endsWith("async"))
                    .collect(Collectors.toMap(NonNullableModel::key, Function.identity()));

                compareMaps(expectedDocsAsync, actualDocsAsync,
                    (expected, actual) -> assertObjectEquals(expected, actual, true));
            })
            .verifyComplete();
    }

    @SuppressWarnings("UseOfObsoleteDateTimeApi")
    @Test
    public void canSearchWithDateInStaticModelSync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);

        OffsetDateTime expected = OffsetDateTime.parse("2010-06-27T00:00:00Z");

        SearchPagedIterable results = client.search("Fancy", new SearchOptions(), Context.NONE);
        Iterator<SearchPagedResponse> iterator = results.iterableByPage().iterator();

        try (SearchPagedResponse result = iterator.next()) {
            assertEquals(1, result.getValue().size());
            Date actual = result.getValue().get(0).getDocument(Hotel.class).lastRenovationDate();
            long epochMilli = expected.toInstant().toEpochMilli();
            assertEquals(new Date(epochMilli), actual);
        }
    }

    @SuppressWarnings("UseOfObsoleteDateTimeApi")
    @Test
    public void canSearchWithDateInStaticModelAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        OffsetDateTime expected = OffsetDateTime.parse("2010-06-27T00:00:00Z");

        StepVerifier.create(asyncClient.search("Fancy", new SearchOptions()).byPage())
            .assertNext(response -> {
                assertEquals(1, response.getValue().size());
                Date actual = response.getValue().get(0).getDocument(Hotel.class).lastRenovationDate();
                long epochMilli = expected.toInstant().toEpochMilli();
                assertEquals(new Date(epochMilli), actual);
            })
            .verifyComplete();
    }

    @Test
    public void canSearchWithSelectedFieldsSync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);

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
        try (SearchPagedResponse result = iterator.next()) {
            assertEquals(2, result.getValue().size());

            // From the result object, extract the two hotels, clean up (irrelevant fields) and change data structure
            // as a preparation to check equality
            Map<String, Object> hotel1 = extractAndTransformSingleResult(result.getValue().get(0));
            Map<String, Object> hotel2 = extractAndTransformSingleResult(result.getValue().get(1));

            assertMapEquals(expectedHotel1, hotel1, true);
            assertMapEquals(expectedHotel2, hotel2, true);
        }
    }

    @Test
    public void canSearchWithSelectedFieldsAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        // Ask JUST for the following two fields
        SearchOptions sp = new SearchOptions();
        sp.setSearchFields("HotelName", "Category");
        sp.setSelect("HotelName", "Rating", "Address/City", "Rooms/Type");

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

        StepVerifier.create(asyncClient.search("fancy luxury secret", sp).byPage())
            .assertNext(response -> {
                assertEquals(2, response.getValue().size());

                // From the result object, extract the two hotels, clean up (irrelevant fields) and change data structure
                // as a preparation to check equality
                Map<String, Object> hotel1 = extractAndTransformSingleResult(response.getValue().get(0));
                Map<String, Object> hotel2 = extractAndTransformSingleResult(response.getValue().get(1));

                assertMapEquals(expectedHotel1, hotel1, true);
                assertMapEquals(expectedHotel2, hotel2, true);
            })
            .verifyComplete();
    }

    @Test
    public void canUseTopAndSkipForClientSidePagingSync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);

        SearchOptions parameters = new SearchOptions().setTop(3).setSkip(0).setOrderBy("HotelId");

        SearchPagedIterable results = client.search("*", parameters, Context.NONE);
        assertKeySequenceEqual(results, Arrays.asList("1", "10", "2"));

        parameters.setSkip(3);
        results = client.search("*", parameters, Context.NONE);
        assertKeySequenceEqual(results, Arrays.asList("3", "4", "5"));
    }

    @Test
    public void canUseTopAndSkipForClientSidePagingAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        SearchOptions parameters = new SearchOptions().setTop(3).setSkip(0).setOrderBy("HotelId");

        StepVerifier.create(getSearchResultsAsync(asyncClient.search("*", parameters, Context.NONE))
                .map(docs -> docs.stream().map(sd -> sd.get("HotelId").toString()).collect(Collectors.toList())))
            .assertNext(actualKeys -> assertEquals(Arrays.asList("1", "10", "2"), actualKeys))
            .verifyComplete();

        parameters.setSkip(3);

        StepVerifier.create(getSearchResultsAsync(asyncClient.search("*", parameters, Context.NONE))
                .map(docs -> docs.stream().map(sd -> sd.get("HotelId").toString()).collect(Collectors.toList())))
            .assertNext(actualKeys -> assertEquals(Arrays.asList("3", "4", "5"), actualKeys))
            .verifyComplete();
    }

    @Test
    public void searchWithoutOrderBySortsByScoreSync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);

        Iterator<SearchResult> results = client.search("*", new SearchOptions().setFilter("Rating lt 4"), Context.NONE)
            .iterator();
        SearchResult firstResult = results.next();
        SearchResult secondResult = results.next();
        assertTrue(firstResult.getScore() <= secondResult.getScore());
    }

    @Test
    public void searchWithoutOrderBySortsByScoreAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        StepVerifier.create(asyncClient.search("*", new SearchOptions().setFilter("Rating lt 4"))
                .take(2).collectList())
            .assertNext(results -> assertTrue(results.get(0).getScore() <= results.get(1).getScore()))
            .verifyComplete();
    }

    @Test
    public void orderByProgressivelyBreaksTiesSync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);

        String[] expectedResults = new String[]{"1", "9", "3", "4", "5", "10", "2", "6", "7", "8"};

        Stream<String> results = client.search("*",
                new SearchOptions().setOrderBy("Rating desc", "LastRenovationDate asc", "HotelId"), Context.NONE)
            .stream()
            .map(SearchTests::getSearchResultId);

        assertArrayEquals(results.toArray(), expectedResults);
    }

    @Test
    public void orderByProgressivelyBreaksTiesAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        String[] expectedResults = new String[]{"1", "9", "3", "4", "5", "10", "2", "6", "7", "8"};

        StepVerifier.create(asyncClient.search("*",
                    new SearchOptions().setOrderBy("Rating desc", "LastRenovationDate asc", "HotelId"))
                .map(SearchTests::getSearchResultId)
                .collectList())
            .assertNext(results -> assertArrayEquals(results.toArray(), expectedResults))
            .verifyComplete();
    }

    @Test
    public void canFilterSync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);

        SearchOptions searchOptions = new SearchOptions()
            .setFilter("Rating gt 3 and LastRenovationDate gt 2000-01-01T00:00:00Z")
            .setOrderBy("HotelId asc");

        SearchPagedIterable results = client.search("*", searchOptions, Context.NONE);
        assertNotNull(results);

        List<SearchDocument> searchResultsList = getSearchResultsSync(results);
        assertEquals(2, searchResultsList.size());
        assertEquals("1", searchResultsList.get(0).get("HotelId").toString());
        assertEquals("5", searchResultsList.get(1).get("HotelId").toString());
    }

    @Test
    public void canFilterAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        SearchOptions searchOptions = new SearchOptions()
            .setFilter("Rating gt 3 and LastRenovationDate gt 2000-01-01T00:00:00Z")
            .setOrderBy("HotelId asc");

        StepVerifier.create(getSearchResultsAsync(asyncClient.search("*", searchOptions)))
            .assertNext(searchResultsList -> {
                assertEquals(2, searchResultsList.size());
                assertEquals("1", searchResultsList.get(0).get("HotelId").toString());
                assertEquals("5", searchResultsList.get(1).get("HotelId").toString());
            })
            .verifyComplete();
    }

    @Test
    @LiveOnly
    public void canSearchWithRangeFacetsSync() {
        // Disable sanitizer `$.to` for this test
        // interceptorManager.removeSanitizers("AZSDK3424"));
        SearchClient client = getClient(HOTEL_INDEX_NAME);

        List<Map<String, Object>> hotels = readJsonFileToList(HOTELS_DATA_JSON);

        for (SearchPagedResponse response : client.search("*", getSearchOptionsForRangeFacets(), Context.NONE)
            .iterableByPage()) {
            Map<String, List<FacetResult>> facets = SearchPagedResponseAccessHelper.getFacets(response);
            assertNotNull(facets);

            List<RangeFacetResult<String>> baseRateFacets = getRangeFacetsForField(facets, "Rooms/BaseRate", 4);
            List<RangeFacetResult<String>> lastRenovationDateFacets = getRangeFacetsForField(facets,
                "LastRenovationDate", 2);
            assertRangeFacets(baseRateFacets, lastRenovationDateFacets);

            assertContainHotelIds(hotels, response.getValue());
        }
    }

    @Test
    @LiveOnly
    public void canSearchWithRangeFacetsAsync() {
        // Disable sanitizer `$.to` for this test
        // interceptorManager.removeSanitizers("AZSDK3424");
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        List<Map<String, Object>> hotels = readJsonFileToList(HOTELS_DATA_JSON);

        StepVerifier.create(asyncClient.search("*", getSearchOptionsForRangeFacets()).byPage())
            .thenConsumeWhile(response -> {
                Map<String, List<FacetResult>> facets = SearchPagedResponseAccessHelper.getFacets(response);
                assertNotNull(facets);

                List<RangeFacetResult<String>> baseRateFacets = getRangeFacetsForField(facets, "Rooms/BaseRate", 4);
                List<RangeFacetResult<String>> lastRenovationDateFacets = getRangeFacetsForField(facets,
                    "LastRenovationDate", 2);
                assertRangeFacets(baseRateFacets, lastRenovationDateFacets);

                assertContainHotelIds(hotels, response.getValue());

                return true;
            })
            .verifyComplete();
    }

    @Test
    public void canSearchWithValueFacetsSync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);

        List<Map<String, Object>> hotels = readJsonFileToList(HOTELS_DATA_JSON);

        for (SearchPagedResponse response : client.search("*", getSearchOptionsForValueFacets(), Context.NONE)
            .iterableByPage()) {
            Map<String, List<FacetResult>> facets = SearchPagedResponseAccessHelper.getFacets(response);
            assertNotNull(facets);

            canSearchWithValueFacetsValidateResponse(response, hotels, facets);
        }
    }

    @Test
    public void canSearchWithValueFacetsAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        List<Map<String, Object>> hotels = readJsonFileToList(HOTELS_DATA_JSON);

        StepVerifier.create(asyncClient.search("*", getSearchOptionsForValueFacets()).byPage())
            .thenConsumeWhile(response -> {
                Map<String, List<FacetResult>> facets = SearchPagedResponseAccessHelper.getFacets(response);
                assertNotNull(facets);

                canSearchWithValueFacetsValidateResponse(response, hotels, facets);

                return true;
            })
            .verifyComplete();
    }

    @Test
    public void canSearchWithLuceneSyntaxSync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);

        Map<String, Object> expectedResult = new HashMap<>();
        expectedResult.put("HotelName", "Roach Motel");
        expectedResult.put("Rating", 1);

        SearchOptions searchOptions = new SearchOptions().setQueryType(QueryType.FULL).setSelect("HotelName", "Rating");

        List<SearchDocument> searchResultsList = getSearchResultsSync(client.search("HotelName:roch~", searchOptions,
            Context.NONE));
        assertEquals(1, searchResultsList.size());
        assertEquals(expectedResult, searchResultsList.get(0));
    }

    @Test
    public void canSearchWithLuceneSyntaxAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        Map<String, Object> expectedResult = new HashMap<>();
        expectedResult.put("HotelName", "Roach Motel");
        expectedResult.put("Rating", 1);

        SearchOptions searchOptions = new SearchOptions().setQueryType(QueryType.FULL).setSelect("HotelName", "Rating");

        StepVerifier.create(getSearchResultsAsync(asyncClient.search("HotelName:roch~", searchOptions)))
            .assertNext(searchResultsList -> {
                assertEquals(1, searchResultsList.size());
                assertEquals(expectedResult, searchResultsList.get(0));
            })
            .verifyComplete();
    }

    @Test
    public void canFilterNonNullableTypeSyncAndAsync() {
        SearchOptions searchOptions = new SearchOptions()
            .setFilter("IntValue eq 0 or (Bucket/BucketName eq 'B' and Bucket/Count lt 10)");

        String indexName = createIndexWithValueTypes();
        indexesToDelete.add(indexName);
        SearchClient client = getSearchClientBuilder(indexName, true).buildClient();

        List<SearchDocument> docsList = createDocsListWithValueTypes("");
        uploadDocuments(client, docsList);

        Map<String, SearchDocument> expectedDocs = docsList.stream()
            .filter(d -> !d.get("Key").equals("789"))
            .collect(Collectors.toMap(sd -> sd.get("Key").toString(), Function.identity()));

        SearchPagedIterable results = client.search("*", searchOptions, Context.NONE);
        assertNotNull(results);

        Map<String, SearchDocument> actualDocs = results.stream()
            .map(sr -> sr.getDocument(SearchDocument.class))
            .collect(Collectors.toMap(sd -> sd.get("Key").toString(), Function.identity()));

        compareMaps(expectedDocs, actualDocs, (expected, actual) -> assertObjectEquals(expected, actual, true));

        SearchAsyncClient asyncClient = getSearchClientBuilder(indexName, false).buildAsyncClient();
        List<SearchDocument> docsListAsync = createDocsListWithValueTypes("async");
        uploadDocuments(asyncClient, docsListAsync);

        Map<String, SearchDocument> expectedDocsAsync = docsListAsync.stream()
            .filter(d -> !d.get("Key").equals("789async"))
            .collect(Collectors.toMap(sd -> sd.get("Key").toString(), Function.identity()));

        StepVerifier.create(asyncClient.search("*", searchOptions)
                .map(sr -> sr.getDocument(SearchDocument.class))
                .filter(doc -> doc.get("Key").toString().endsWith("async"))
                .collectMap(sd -> sd.get("Key").toString()))
            .assertNext(resultsAsync -> compareMaps(expectedDocsAsync, resultsAsync,
                (expected, actual) -> assertObjectEquals(expected, actual, true)))
            .verifyComplete();
    }

    @Test
    public void canSearchWithSearchModeAllSync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);

        List<SearchDocument> response = getSearchResultsSync(client.search("Cheapest hotel",
            new SearchOptions().setQueryType(QueryType.SIMPLE).setSearchMode(SearchMode.ALL), Context.NONE));

        assertEquals(1, response.size());
        assertEquals("2", response.get(0).get("HotelId"));
    }

    @Test
    public void canSearchWithSearchModeAllAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        StepVerifier.create(getSearchResultsAsync(asyncClient.search("Cheapest hotel",
                new SearchOptions().setQueryType(QueryType.SIMPLE).setSearchMode(SearchMode.ALL))))
            .assertNext(response -> {
                assertEquals(1, response.size());
                assertEquals("2", response.get(0).get("HotelId"));
            })
            .verifyComplete();
    }

    @Test
    public void defaultSearchModeIsAnySync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);

        List<SearchDocument> response = getSearchResultsSync(client.search("Cheapest hotel",
            new SearchOptions().setOrderBy("HotelId"), Context.NONE));
        assertEquals(7, response.size());
        assertEquals(Arrays.asList("1", "10", "2", "3", "4", "5", "9"),
            response.stream().map(res -> res.get("HotelId").toString()).collect(Collectors.toList()));
    }

    @Test
    public void defaultSearchModeIsAnyAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        StepVerifier.create(getSearchResultsAsync(asyncClient.search("Cheapest hotel",
                new SearchOptions().setOrderBy("HotelId"))))
            .assertNext(response -> {
                assertEquals(7, response.size());
                assertEquals(Arrays.asList("1", "10", "2", "3", "4", "5", "9"),
                    response.stream().map(res -> res.get("HotelId").toString()).collect(Collectors.toList()));
            })
            .verifyComplete();
    }

    @Test
    public void canGetResultCountInSearchSync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);
        List<Map<String, Object>> hotels = readJsonFileToList(HOTELS_DATA_JSON);

        SearchPagedIterable results = client.search("*", new SearchOptions().setIncludeTotalCount(true), Context.NONE);
        assertNotNull(results);
        assertEquals(hotels.size(), results.getTotalCount().intValue());

        Iterator<SearchPagedResponse> iterator = results.iterableByPage().iterator();

        assertNotNull(iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    public void canGetResultCountInSearchAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);
        List<Map<String, Object>> hotels = readJsonFileToList(HOTELS_DATA_JSON);

        StepVerifier.create(asyncClient.search("*", new SearchOptions().setIncludeTotalCount(true)).byPage())
            .assertNext(response -> assertEquals(hotels.size(), SearchPagedResponseAccessHelper.getCount(response)))
            .verifyComplete();
    }

    @Test
    public void canSearchWithRegexSync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);

        SearchOptions searchOptions = new SearchOptions()
            .setQueryType(QueryType.FULL)
            .setSelect("HotelName", "Rating");

        SearchPagedIterable results = client.search("HotelName:/.*oach.*\\/?/", searchOptions,
            Context.NONE);
        assertNotNull(results);

        List<SearchDocument> resultsList = getSearchResultsSync(results);

        SearchDocument expectedHotel = new SearchDocument();
        expectedHotel.put("HotelName", "Roach Motel");
        expectedHotel.put("Rating", 1);

        assertEquals(1, resultsList.size());
        assertEquals(resultsList.get(0), expectedHotel);
    }

    @Test
    public void canSearchWithRegexAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        SearchOptions searchOptions = new SearchOptions()
            .setQueryType(QueryType.FULL)
            .setSelect("HotelName", "Rating");

        StepVerifier.create(getSearchResultsAsync(asyncClient.search("HotelName:/.*oach.*\\/?/", searchOptions)))
            .assertNext(resultsList -> {
                Map<String, Object> expectedHotel = new HashMap<>();
                expectedHotel.put("HotelName", "Roach Motel");
                expectedHotel.put("Rating", 1);

                assertEquals(1, resultsList.size());
                assertEquals(resultsList.get(0), expectedHotel);
            })
            .verifyComplete();
    }

    @Test
    public void canSearchWithEscapedSpecialCharsInRegexSync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);

        SearchOptions searchOptions = new SearchOptions().setQueryType(QueryType.FULL);

        SearchPagedIterable results = client.search("\\+\\-\\&\\|\\!\\(\\)\\{\\}\\[\\]\\^\\~\\*\\?\\:", searchOptions,
            Context.NONE);
        assertNotNull(results);

        List<SearchDocument> resultsList = getSearchResultsSync(results);
        assertEquals(0, resultsList.size());
    }

    @Test
    public void canSearchWithEscapedSpecialCharsInRegexAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        SearchOptions searchOptions = new SearchOptions().setQueryType(QueryType.FULL);

        StepVerifier.create(getSearchResultsAsync(asyncClient.search("\\+\\-\\&\\|\\!\\(\\)\\{\\}\\[\\]\\^\\~\\*\\?\\:",
                searchOptions)))
            .assertNext(response -> assertEquals(0, response.size()))
            .verifyComplete();
    }

    @Test
    public void searchWithScoringProfileBoostsScoreSync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);

        SearchOptions searchOptions = new SearchOptions()
            .setScoringProfile("nearest")
            .setScoringParameters(new ScoringParameter("myloc", new GeoPoint(-122.0, 49.0)))
            .setFilter("Rating eq 5 or Rating eq 1")
            .setOrderBy("HotelId desc");

        List<SearchDocument> response = getSearchResultsSync(client.search("hotel", searchOptions, Context.NONE));
        assertEquals(2, response.size());
        assertEquals("2", response.get(0).get("HotelId").toString());
        assertEquals("1", response.get(1).get("HotelId").toString());
    }

    @Test
    public void searchWithScoringProfileBoostsScoreAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        SearchOptions searchOptions = new SearchOptions()
            .setScoringProfile("nearest")
            .setScoringParameters(new ScoringParameter("myloc", new GeoPoint(-122.0, 49.0)))
            .setFilter("Rating eq 5 or Rating eq 1")
            .setOrderBy("HotelId desc");

        StepVerifier.create(getSearchResultsAsync(asyncClient.search("hotel", searchOptions)))
            .assertNext(response -> {
                assertEquals(2, response.size());
                assertEquals("2", response.get(0).get("HotelId").toString());
                assertEquals("1", response.get(1).get("HotelId").toString());
            })
            .verifyComplete();
    }

    @Test
    public void searchWithScoringProfileEscaperSync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);

        SearchOptions searchOptions = new SearchOptions()
            .setScoringProfile("text")
            .setScoringParameters(new ScoringParameter("mytag", Arrays.asList("concierge", "Hello, O''Brien")))
            .setFilter("Rating eq 5 or Rating eq 1");

        List<SearchDocument> response = getSearchResultsSync(client.search("hotel", searchOptions, Context.NONE));
        assertEquals(2, response.size());
        assertEquals(Arrays.asList("1", "2"),
            response.stream().map(res -> res.get("HotelId").toString()).collect(Collectors.toList()));
    }

    @Test
    public void searchWithScoringProfileEscaperAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        SearchOptions searchOptions = new SearchOptions()
            .setScoringProfile("text")
            .setScoringParameters(new ScoringParameter("mytag", Arrays.asList("concierge", "Hello, O''Brien")))
            .setFilter("Rating eq 5 or Rating eq 1");

        StepVerifier.create(getSearchResultsAsync(asyncClient.search("hotel", searchOptions)))
            .assertNext(response -> {
                assertEquals(2, response.size());
                assertEquals(Arrays.asList("1", "2"),
                    response.stream().map(res -> res.get("HotelId").toString()).collect(Collectors.toList()));
            })
            .verifyComplete();
    }

    @Test
    public void searchWithScoringParametersEmptySync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);

        SearchOptions searchOptions = new SearchOptions()
            .setScoringProfile("text")
            .setScoringParameters(new ScoringParameter("mytag", Arrays.asList("", "concierge")))
            .setFilter("Rating eq 5 or Rating eq 1");

        List<SearchDocument> response = getSearchResultsSync(client.search("hotel", searchOptions, Context.NONE));
        assertEquals(2, response.size());
        assertEquals(Arrays.asList("1", "2"),
            response.stream().map(res -> res.get("HotelId").toString()).collect(Collectors.toList()));
    }

    @Test
    public void searchWithScoringParametersEmptyAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        SearchOptions searchOptions = new SearchOptions()
            .setScoringProfile("text")
            .setScoringParameters(new ScoringParameter("mytag", Arrays.asList("", "concierge")))
            .setFilter("Rating eq 5 or Rating eq 1");

        StepVerifier.create(getSearchResultsAsync(asyncClient.search("hotel", searchOptions)))
            .assertNext(response -> {
                assertEquals(2, response.size());
                assertEquals(Arrays.asList("1", "2"),
                    response.stream().map(res -> res.get("HotelId").toString()).collect(Collectors.toList()));
            })
            .verifyComplete();
    }

    @Test
    public void canSearchWithMinimumCoverageSync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);

        SearchPagedResponse response = client.search("*", new SearchOptions().setMinimumCoverage(50.0), Context.NONE)
            .iterableByPage().iterator().next();

        assertEquals(100.0, SearchPagedResponseAccessHelper.getCoverage(response), 0);
    }

    @Test
    public void canSearchWithMinimumCoverageAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        StepVerifier.create(asyncClient.search("*", new SearchOptions().setMinimumCoverage(50.0)).byPage())
            .assertNext(response -> assertEquals(100.0, SearchPagedResponseAccessHelper.getCoverage(response)))
            .verifyComplete();
    }

    @Test
    public void canUseHitHighlightingSync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);

        //arrange
        String description = "Description";
        String category = "Category";

        SearchOptions sp = new SearchOptions();
        sp.setFilter("Rating eq 5");
        sp.setHighlightPreTag("<b>");
        sp.setHighlightPostTag("</b>");
        sp.setHighlightFields(category, description);

        //act
        try (SearchPagedResponse result = client.search("luxury hotel", sp, Context.NONE).iterableByPage().iterator()
            .next()) {

            List<SearchResult> documents = result.getValue();
            assertEquals(1, documents.size());

            Map<String, List<String>> highlights = documents.get(0).getHighlights();
            assertEquals(2, highlights.keySet().size());
            assertTrue(highlights.containsKey(description));
            assertTrue(highlights.containsKey(category));

            //asserts
            assertEquals("<b>Luxury</b>", highlights.get(category).get(0));

            List<String> expectedDescriptionHighlights = Arrays.asList(
                "Best <b>hotel</b> in town if you like <b>luxury</b> <b>hotels</b>.",
                "We highly recommend this <b>hotel</b>.");

            assertEquals(expectedDescriptionHighlights, highlights.get(description));
        }
    }

    @Test
    public void canUseHitHighlightingAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        //arrange
        String description = "Description";
        String category = "Category";

        SearchOptions sp = new SearchOptions();
        sp.setFilter("Rating eq 5");
        sp.setHighlightPreTag("<b>");
        sp.setHighlightPostTag("</b>");
        sp.setHighlightFields(category, description);

        StepVerifier.create(asyncClient.search("luxury hotel", sp).byPage())
            .assertNext(result -> {
                List<SearchResult> documents = result.getValue();

                assertEquals(1, documents.size());
                Map<String, List<String>> highlights = documents.get(0).getHighlights();
                assertEquals(2, highlights.keySet().size());
                assertTrue(highlights.containsKey(description));
                assertTrue(highlights.containsKey(category));

                assertEquals("<b>Luxury</b>", highlights.get(category).get(0));

                List<String> expectedDescriptionHighlights = Arrays.asList(
                    "Best <b>hotel</b> in town if you like <b>luxury</b> <b>hotels</b>.",
                    "We highly recommend this <b>hotel</b>.");

                assertEquals(expectedDescriptionHighlights, highlights.get(description));
            })
            .verifyComplete();
    }

    @Test
    public void canSearchWithSynonymsSync() {
        SearchClient client = getClient(SYNONYM_INDEX_NAME);

        SearchOptions searchOptions = new SearchOptions()
            .setQueryType(QueryType.FULL)
            .setSearchFields("HotelName")
            .setSelect("HotelName", "Rating");

        SearchPagedIterable results = client.search("luxury", searchOptions, Context.NONE);
        assertNotNull(results);

        List<SearchDocument> response = getSearchResultsSync(results);
        assertEquals(1, response.size());
        assertEquals("Fancy Stay", response.get(0).get("HotelName"));
        assertEquals(5, response.get(0).get("Rating"));
    }

    @Test
    public void canSearchWithSynonymsAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(SYNONYM_INDEX_NAME);

        SearchOptions searchOptions = new SearchOptions()
            .setQueryType(QueryType.FULL)
            .setSearchFields("HotelName")
            .setSelect("HotelName", "Rating");

        StepVerifier.create(getSearchResultsAsync(asyncClient.search("luxury", searchOptions)))
            .assertNext(results -> {
                assertEquals(1, results.size());
                assertEquals("Fancy Stay", results.get(0).get("HotelName"));
                assertEquals(5, results.get(0).get("Rating"));
            })
            .verifyComplete();
    }

    private static List<SearchDocument> getSearchResultsSync(SearchPagedIterable results) {
        return results.stream().map(sr -> sr.getDocument(SearchDocument.class)).collect(Collectors.toList());
    }

    private static Mono<List<SearchDocument>> getSearchResultsAsync(SearchPagedFlux results) {
        return results.map(sr -> sr.getDocument(SearchDocument.class)).collectList();
    }

    private static Map<String, Object> extractAndTransformSingleResult(SearchResult result) {
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

    private static void assertKeySequenceEqual(SearchPagedIterable results, List<String> expectedKeys) {
        assertNotNull(results);

        List<String> actualKeys = results.stream().map(doc -> doc.getDocument(SearchDocument.class))
            .filter(sd -> sd.containsKey("HotelId"))
            .map(sd -> sd.get("HotelId").toString())
            .collect(Collectors.toList());

        assertEquals(expectedKeys, actualKeys);
    }

    static List<Map<String, Object>> createHotelsList() {
        List<Map<String, Object>> documents = new ArrayList<>();
        for (int i = 1; i <= 3000; i++) {
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

    static <T> void assertRangeFacets(List<RangeFacetResult<T>> baseRateFacets,
        List<RangeFacetResult<T>> lastRenovationDateFacets) {
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

    static <T> List<RangeFacetResult<T>> getRangeFacetsForField(Map<String, List<FacetResult>> facets,
        String expectedField, int expectedCount) {
        List<FacetResult> facetCollection = getFacetsForField(facets, expectedField, expectedCount);
        return facetCollection.stream().map(RangeFacetResult<T>::new).collect(Collectors.toList());
    }

    static <T> List<ValueFacetResult<T>> getValueFacetsForField(Map<String, List<FacetResult>> facets,
        String expectedField, int expectedCount) {
        List<FacetResult> facetCollection = getFacetsForField(facets, expectedField, expectedCount);
        return facetCollection.stream().map(ValueFacetResult<T>::new)
            .collect(Collectors.toList());
    }

    private static List<FacetResult> getFacetsForField(Map<String, List<FacetResult>> facets, String expectedField,
        int expectedCount) {
        assertTrue(facets.containsKey(expectedField));
        List<FacetResult> results = facets.get(expectedField);
        assertEquals(expectedCount, results.size());
        return results;
    }

    static void assertContainHotelIds(List<Map<String, Object>> expected, List<SearchResult> actual) {
        assertNotNull(actual);
        Set<String> actualKeys = actual.stream().filter(item -> item.getDocument(SearchDocument.class)
            .containsKey("HotelId")).map(item -> (String) item.getDocument(SearchDocument.class)
            .get("HotelId")).collect(Collectors.toSet());
        Set<String> expectedKeys = expected.stream().filter(item -> item.containsKey("HotelId"))
            .map(item -> (String) item.get("HotelId")).collect(Collectors.toSet());
        assertEquals(expectedKeys, actualKeys);
    }

    static <T> void assertValueFacetsEqual(List<ValueFacetResult<T>> actualFacets,
        ArrayList<ValueFacetResult<T>> expectedFacets) {
        assertEquals(expectedFacets.size(), actualFacets.size());
        for (int i = 0; i < actualFacets.size(); i++) {
            assertEquals(expectedFacets.get(i).getCount(), actualFacets.get(i).getCount());
            assertEquals(expectedFacets.get(i).getValue(), actualFacets.get(i).getValue());
        }
    }

    static String getSearchResultId(SearchResult searchResult) {
        return searchResult.getDocument(SearchDocument.class).get("HotelId").toString();
    }

    static SearchOptions getSearchOptionsForRangeFacets() {
        return new SearchOptions().setFacets("Rooms/BaseRate,values:5|8|10",
            "LastRenovationDate,values:2000-01-01T00:00:00Z");
    }

    static SearchOptions getSearchOptionsForValueFacets() {
        return new SearchOptions().setFacets("Rating,count:2,sort:-value",
            "SmokingAllowed,sort:count",
            "Category",
            "LastRenovationDate,interval:year",
            "Rooms/BaseRate,sort:value",
            "Tags,sort:value");
    }

    static void assertListEqualHotelIds(List<String> expected, List<SearchResult> actual) {
        assertNotNull(actual);
        List<String> actualKeys = actual.stream().filter(item -> item.getDocument(SearchDocument.class)
            .containsKey("HotelId")).map(item -> (String) item.getDocument(SearchDocument.class)
            .get("HotelId")).collect(Collectors.toList());
        assertEquals(expected, actualKeys);
    }

    String createIndexWithNonNullableTypes() {
        SearchIndex index = new SearchIndex(testResourceNamer.randomName("non-nullable-index", 64))
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
        SearchIndex index = new SearchIndex(testResourceNamer.randomName("testindex", 64))
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

    static List<SearchDocument> createDocsListWithValueTypes(String keySuffix) {
        SearchDocument element1 = new SearchDocument();
        element1.put("Key", "123" + keySuffix);
        element1.put("IntValue", 0);

        Map<String, Object> subElement1 = new HashMap<>();
        subElement1.put("BucketName", "A");
        subElement1.put("Count", 3);
        element1.put("Bucket", subElement1);

        SearchDocument element2 = new SearchDocument();
        element2.put("Key", "456" + keySuffix);
        element2.put("IntValue", 7);

        Map<String, Object> subElement2 = new HashMap<>();
        subElement2.put("BucketName", "B");
        subElement2.put("Count", 5);
        element2.put("Bucket", subElement2);

        SearchDocument element3 = new SearchDocument();
        element3.put("Key", "789" + keySuffix);
        element3.put("IntValue", 1);

        Map<String, Object> subElement3 = new HashMap<>();
        subElement3.put("BucketName", "B");
        subElement3.put("Count", 99);
        element3.put("Bucket", subElement3);

        return Arrays.asList(element1, element2, element3);
    }

    private static void canSearchWithValueFacetsValidateResponse(SearchPagedResponse result,
        List<Map<String, Object>> expectedHotels, Map<String, List<FacetResult>> facets) {
        assertContainHotelIds(expectedHotels, result.getValue());

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
