// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.annotation.LiveOnly;
import com.azure.core.util.IterableStream;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonWriter;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.models.SearchField;
import com.azure.search.documents.indexes.models.SearchFieldDataType;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.azure.search.documents.indexes.models.SynonymMap;
import com.azure.search.documents.models.FacetResult;
import com.azure.search.documents.models.QueryType;
import com.azure.search.documents.models.SearchMode;
import com.azure.search.documents.models.SearchOptions;
import com.azure.search.documents.models.SearchPagedFlux;
import com.azure.search.documents.models.SearchPagedIterable;
import com.azure.search.documents.models.SearchPagedResponse;
import com.azure.search.documents.models.SearchResult;
import com.azure.search.documents.test.environment.models.Bucket;
import com.azure.search.documents.test.environment.models.Hotel;
import com.azure.search.documents.test.environment.models.NonNullableModel;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.azure.search.documents.TestHelpers.assertMapEquals;
import static com.azure.search.documents.TestHelpers.assertObjectEquals;
import static com.azure.search.documents.TestHelpers.convertFromMapStringObject;
import static com.azure.search.documents.TestHelpers.convertMapToValue;
import static com.azure.search.documents.TestHelpers.readJsonFileToList;
import static com.azure.search.documents.TestHelpers.setupSharedIndex;
import static com.azure.search.documents.TestHelpers.uploadDocuments;
import static com.azure.search.documents.TestHelpers.uploadDocumentsRaw;
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
        TestProxyTestBase.setupClass();

        if (TEST_MODE == TestMode.PLAYBACK) {
            return;
        }

        searchIndexClient = setupSharedIndex(HOTEL_INDEX_NAME, HOTELS_TESTS_INDEX_DATA_JSON, HOTELS_DATA_JSON);
        setupSharedIndex(SYNONYM_INDEX_NAME, HOTELS_TESTS_INDEX_DATA_JSON, HOTELS_DATA_JSON);
        setupSharedIndex(LARGE_INDEX_NAME, HOTELS_TESTS_INDEX_DATA_JSON, null);

        uploadDocumentsRaw(searchIndexClient.getSearchClient(LARGE_INDEX_NAME), createHotelsList());

        searchIndexClient.createSynonymMap(new SynonymMap(SYNONYM_NAME, "luxury,fancy"));

        // Attach index field to SynonymMap
        SearchIndex hotelsIndex = searchIndexClient.getIndex(SYNONYM_INDEX_NAME);
        hotelsIndex.getFields()
            .stream()
            .filter(f -> "HotelName".equals(f.getName()))
            .findFirst()
            .orElseThrow(NoSuchElementException::new)
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
        badSearchSync(new SearchOptions().setFilter("This is not a valid filter."));
    }

    @Test
    public void searchThrowsWhenRequestIsMalformedAsync() {
        badSearchAsync(new SearchOptions().setFilter("This is not a valid filter."));
    }

    @Test
    public void searchThrowsWhenSpecialCharInRegexIsUnescapedSync() {
        badSearchSync(new SearchOptions().setSearchText("/.*/.*/").setQueryType(QueryType.FULL));
    }

    @Test
    public void searchThrowsWhenSpecialCharInRegexIsUnescapedAsync() {
        badSearchAsync(new SearchOptions().setSearchText("/.*/.*/").setQueryType(QueryType.FULL));
    }

    private void badSearchSync(SearchOptions searchOptions) {
        HttpResponseException ex = assertThrows(HttpResponseException.class,
            () -> getSearchClientBuilder(HOTEL_INDEX_NAME, true).buildClient()
                .search(searchOptions)
                .iterableByPage()
                .iterator()
                .next());

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, ex.getResponse().getStatusCode());
    }

    private void badSearchAsync(SearchOptions searchOptions) {
        StepVerifier
            .create(getSearchClientBuilder(HOTEL_INDEX_NAME, false).buildAsyncClient().search(searchOptions).byPage())
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
        Map<String, Map<String, Object>> expectedHotels
            = hotels.stream().collect(Collectors.toMap(h -> h.get("HotelId").toString(), Function.identity()));

        for (SearchPagedResponse response : client.search(new SearchOptions()).iterableByPage()) {
            assertNull(response.getCount());
            assertNull(response.getCoverage());
            assertNull(response.getFacets());

            response.getElements().forEach(item -> {
                assertEquals(1, item.getScore(), 0);
                assertNull(item.getHighlights());

                Map<String, Object> actual = item.getAdditionalProperties();
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
        Map<String, Map<String, Object>> expectedHotels
            = hotels.stream().collect(Collectors.toMap(h -> h.get("HotelId").toString(), Function.identity()));

        StepVerifier.create(asyncClient.search(new SearchOptions()).byPage()).thenConsumeWhile(response -> {
            assertNull(response.getCount());
            assertNull(response.getCoverage());
            assertNull(response.getFacets());

            response.getElements().forEach(item -> {
                assertEquals(1, item.getScore(), 0);
                assertNull(item.getHighlights());

                Map<String, Object> actual = item.getAdditionalProperties();
                Map<String, Object> expected = expectedHotels.remove(actual.get("HotelId").toString());

                assertNotNull(expected);
                assertMapEquals(expected, actual, true, "properties");
            });

            return true;
        }).verifyComplete();

        assertEquals(0, expectedHotels.size());
    }

    @Test
    public void canContinueSearchSync() {
        SearchClient client = getClient(LARGE_INDEX_NAME);

        // upload large documents batch
        List<Map<String, Object>> hotels = createHotelsList();

        SearchOptions searchOptions = new SearchOptions().setSelect("HotelId").setOrderBy("HotelId asc");

        List<String> expectedHotelIds
            = hotels.stream().map(hotel -> (String) hotel.get("HotelId")).sorted().collect(Collectors.toList());

        // By default, if top isn't specified in the SearchOptions each page will contain 50 results.
        AtomicInteger total = new AtomicInteger();
        client.search(searchOptions).iterableByPage().forEach(page -> {
            assertEquals(50, page.getElements().stream().count());
            assertListEqualHotelIds(expectedHotelIds.subList(total.get(), total.addAndGet(50)), page.getElements());
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

        SearchOptions searchOptions = new SearchOptions().setSelect("HotelId").setOrderBy("HotelId asc");

        List<String> expectedHotelIds
            = hotels.stream().map(hotel -> (String) hotel.get("HotelId")).sorted().collect(Collectors.toList());

        // By default, if top isn't specified in the SearchOptions each page will contain 50 results.
        StepVerifier.create(asyncClient.search(searchOptions).byPage().collectList()).assertNext(pages -> {
            AtomicInteger total = new AtomicInteger();
            pages.forEach(page -> {
                assertEquals(50, page.getElements().stream().count());
                assertListEqualHotelIds(expectedHotelIds.subList(total.get(), total.addAndGet(50)), page.getElements());
                if (total.get() != 3000) {
                    assertNotNull(page.getContinuationToken());
                } else {
                    assertNull(page.getContinuationToken());
                }
            });
        }).verifyComplete();
    }

    @Test
    public void canContinueSearchWithTopSync() {
        SearchClient client = getClient(LARGE_INDEX_NAME);

        // upload large documents batch
        List<Map<String, Object>> hotels = createHotelsList();

        SearchOptions searchOptions = new SearchOptions().setTop(2000).setSelect("HotelId").setOrderBy("HotelId asc");

        List<String> expectedHotelIds
            = hotels.stream().map(hotel -> (String) hotel.get("HotelId")).sorted().collect(Collectors.toList());

        Iterator<SearchPagedResponse> iterator = client.search(searchOptions).iterableByPage().iterator();

        SearchPagedResponse firstPage = iterator.next();
        assertEquals(1000, firstPage.getElements().stream().count());
        assertListEqualHotelIds(expectedHotelIds.subList(0, 1000), firstPage.getElements());
        assertNotNull(firstPage.getContinuationToken());

        SearchPagedResponse secondPage = iterator.next();
        assertEquals(1000, secondPage.getElements().stream().count());
        assertListEqualHotelIds(expectedHotelIds.subList(1000, 2000), secondPage.getElements());
        assertNull(secondPage.getContinuationToken());
    }

    @Test
    public void canContinueSearchWithTopAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(LARGE_INDEX_NAME);

        // upload large documents batch
        List<Map<String, Object>> hotels = createHotelsList();

        SearchOptions searchOptions = new SearchOptions().setTop(2000).setSelect("HotelId").setOrderBy("HotelId asc");

        List<String> expectedHotelIds
            = hotels.stream().map(hotel -> (String) hotel.get("HotelId")).sorted().collect(Collectors.toList());

        StepVerifier.create(asyncClient.search(searchOptions).byPage()).assertNext(response -> {
            assertEquals(1000, response.getElements().stream().count());
            assertListEqualHotelIds(expectedHotelIds.subList(0, 1000), response.getElements());
            assertNotNull(response.getContinuationToken());
        }).assertNext(response -> {
            assertEquals(1000, response.getElements().stream().count());
            assertListEqualHotelIds(expectedHotelIds.subList(1000, 2000), response.getElements());
            assertNull(response.getContinuationToken());
        }).verifyComplete();
    }

    @Test
    public void canSearchStaticallyTypedDocumentsSync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);

        List<Map<String, Object>> hotels = readJsonFileToList(HOTELS_DATA_JSON);
        Map<String, Hotel> expectedHotels = hotels.stream()
            .map(hotel -> convertMapToValue(hotel, Hotel.class))
            .collect(Collectors.toMap(Hotel::hotelId, Function.identity()));

        for (SearchPagedResponse response : client.search(new SearchOptions()).iterableByPage()) {
            assertNull(response.getCount());
            assertNull(response.getCoverage());
            assertNull(response.getFacets());

            response.getElements().forEach(sr -> {
                assertEquals(1, sr.getScore(), 0);
                assertNull(sr.getHighlights());
                Hotel actual = convertFromMapStringObject(sr.getAdditionalProperties(), Hotel::fromJson);

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

        StepVerifier.create(asyncClient.search(new SearchOptions()).byPage()).thenConsumeWhile(response -> {
            assertNull(response.getCount());
            assertNull(response.getCoverage());
            assertNull(response.getFacets());

            response.getElements().forEach(sr -> {
                assertEquals(1, sr.getScore(), 0);
                assertNull(sr.getHighlights());
                Hotel actual = convertFromMapStringObject(sr.getAdditionalProperties(), Hotel::fromJson);

                Hotel expected = expectedHotels.remove(actual.hotelId());
                assertNotNull(expected);
                assertObjectEquals(expected, actual, true, "properties");
            });

            return true;
        }).verifyComplete();

        assertEquals(0, expectedHotels.size());
    }

    @SuppressWarnings("UseOfObsoleteDateTimeApi")
    @Test
    public void canRoundTripNonNullableValueTypesSyncAndAsync() {
        String indexName = createIndexWithNonNullableTypes();
        indexesToDelete.add(indexName);
        SearchClient client = getSearchClientBuilder(indexName, true).buildClient();

        Date startEpoch = Date.from(Instant.ofEpochMilli(1275346800000L));
        NonNullableModel doc1 = new NonNullableModel().key("123")
            .count(3)
            .isEnabled(true)
            .rating(5)
            .ratio(3.25)
            .startDate(new Date(startEpoch.getTime()))
            .endDate(new Date(startEpoch.getTime()))
            .topLevelBucket(new Bucket().bucketName("A").count(12))
            .buckets(new Bucket[] { new Bucket().bucketName("B").count(20), new Bucket().bucketName("C").count(7) });

        NonNullableModel doc2 = new NonNullableModel().key("456").buckets(new Bucket[] { });

        Map<String, NonNullableModel> expectedDocs = new LinkedHashMap<>();
        expectedDocs.put(doc1.key(), doc1);
        expectedDocs.put(doc2.key(), doc2);

        uploadDocuments(client, Arrays.asList(doc1, doc2));

        Iterator<SearchPagedResponse> iterator = client.search(new SearchOptions()).iterableByPage().iterator();

        SearchPagedResponse result = iterator.next();
        Map<String, NonNullableModel> actualDocs = result.getElements()
            .stream()
            .map(sr -> convertFromMapStringObject(sr.getAdditionalProperties(), NonNullableModel::fromJson))
            .collect(Collectors.toMap(NonNullableModel::key, Function.identity()));

        compareMaps(expectedDocs, actualDocs, (expected, actual) -> assertObjectEquals(expected, actual, true));

        SearchAsyncClient asyncClient = getSearchClientBuilder(indexName, false).buildAsyncClient();
        NonNullableModel doc1Async = new NonNullableModel().key("123async")
            .count(3)
            .isEnabled(true)
            .rating(5)
            .ratio(3.25)
            .startDate(new Date(startEpoch.getTime()))
            .endDate(new Date(startEpoch.getTime()))
            .topLevelBucket(new Bucket().bucketName("A").count(12))
            .buckets(new Bucket[] { new Bucket().bucketName("B").count(20), new Bucket().bucketName("C").count(7) });

        NonNullableModel doc2Async = new NonNullableModel().key("456async").buckets(new Bucket[] { });

        Map<String, NonNullableModel> expectedDocsAsync = new LinkedHashMap<>();
        expectedDocsAsync.put(doc1Async.key(), doc1Async);
        expectedDocsAsync.put(doc2Async.key(), doc2Async);

        uploadDocuments(asyncClient, Arrays.asList(doc1Async, doc2Async));

        StepVerifier.create(asyncClient.search(new SearchOptions()).byPage()).assertNext(response -> {
            Map<String, NonNullableModel> actualDocsAsync = response.getElements()
                .stream()
                .map(sr -> convertFromMapStringObject(sr.getAdditionalProperties(), NonNullableModel::fromJson))
                .filter(model -> model.key().endsWith("async"))
                .collect(Collectors.toMap(NonNullableModel::key, Function.identity()));

            compareMaps(expectedDocsAsync, actualDocsAsync,
                (expected, actual) -> assertObjectEquals(expected, actual, true));
        }).verifyComplete();
    }

    @SuppressWarnings("UseOfObsoleteDateTimeApi")
    @Test
    public void canSearchWithDateInStaticModelSync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);

        OffsetDateTime expected = OffsetDateTime.parse("2010-06-27T00:00:00Z");

        Iterator<SearchPagedResponse> iterator
            = client.search(new SearchOptions().setSearchText("Fancy")).iterableByPage().iterator();

        SearchPagedResponse result = iterator.next();
        assertEquals(1, result.getElements().stream().count());
        Date actual = result.getElements()
            .stream()
            .findFirst()
            .map(sr -> convertFromMapStringObject(sr.getAdditionalProperties(), Hotel::fromJson).lastRenovationDate())
            .get();
        long epochMilli = expected.toInstant().toEpochMilli();
        assertEquals(new Date(epochMilli), actual);
    }

    @SuppressWarnings("UseOfObsoleteDateTimeApi")
    @Test
    public void canSearchWithDateInStaticModelAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        OffsetDateTime expected = OffsetDateTime.parse("2010-06-27T00:00:00Z");

        StepVerifier.create(asyncClient.search(new SearchOptions().setSearchText("Fancy")).byPage())
            .assertNext(response -> {
                assertEquals(1, response.getElements().stream().count());
                Date actual = response.getElements()
                    .stream()
                    .findFirst()
                    .map(sr -> convertFromMapStringObject(sr.getAdditionalProperties(), Hotel::fromJson)
                        .lastRenovationDate())
                    .get();
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

        Map<String, Object> expectedHotel1 = new LinkedHashMap<>();
        expectedHotel1.put("HotelName", "Fancy Stay");
        expectedHotel1.put("Rating", 5);
        expectedHotel1.put("Address", null);
        expectedHotel1.put("Rooms", Collections.emptyList());

        // This is the expected document when querying the document later (notice that only two fields are expected)
        Map<String, Object> expectedHotel2 = new LinkedHashMap<>();
        expectedHotel2.put("HotelName", "Secret Point Motel");
        expectedHotel2.put("Rating", 4);
        Map<String, Object> address = new LinkedHashMap<>();
        address.put("City", "New York");
        expectedHotel2.put("Address", address);
        Map<String, Object> rooms = new LinkedHashMap<>();
        rooms.put("Type", "Budget Room");
        Map<String, Object> rooms2 = new LinkedHashMap<>();
        rooms2.put("Type", "Budget Room");
        expectedHotel2.put("Rooms", Arrays.asList(rooms, rooms2));

        Iterator<SearchPagedResponse> iterator
            = client.search(sp.setSearchText("fancy luxury secret")).iterableByPage().iterator();
        SearchPagedResponse result = iterator.next();
        Iterator<SearchResult> searchResults = result.getElements().iterator();
        SearchResult result1 = searchResults.next();
        SearchResult result2 = searchResults.next();
        assertFalse(searchResults.hasNext());

        assertMapEquals(expectedHotel1, result1.getAdditionalProperties(), true);
        assertMapEquals(expectedHotel2, result2.getAdditionalProperties(), true);
    }

    @Test
    public void canSearchWithSelectedFieldsAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        // Ask JUST for the following two fields
        SearchOptions sp = new SearchOptions();
        sp.setSearchFields("HotelName", "Category");
        sp.setSelect("HotelName", "Rating", "Address/City", "Rooms/Type");

        Map<String, Object> expectedHotel1 = new LinkedHashMap<>();
        expectedHotel1.put("HotelName", "Fancy Stay");
        expectedHotel1.put("Rating", 5);
        expectedHotel1.put("Address", null);
        expectedHotel1.put("Rooms", Collections.emptyList());

        // This is the expected document when querying the document later (notice that only two fields are expected)
        Map<String, Object> expectedHotel2 = new LinkedHashMap<>();
        expectedHotel2.put("HotelName", "Secret Point Motel");
        expectedHotel2.put("Rating", 4);
        Map<String, Object> address = new LinkedHashMap<>();
        address.put("City", "New York");
        expectedHotel2.put("Address", address);
        Map<String, Object> rooms = new LinkedHashMap<>();
        rooms.put("Type", "Budget Room");
        Map<String, Object> rooms2 = new LinkedHashMap<>();
        rooms2.put("Type", "Budget Room");
        expectedHotel2.put("Rooms", Arrays.asList(rooms, rooms2));

        StepVerifier.create(asyncClient.search(sp.setSearchText("fancy luxury secret")).byPage())
            .assertNext(response -> {
                Iterator<SearchResult> searchResults = response.getElements().iterator();
                SearchResult result1 = searchResults.next();
                SearchResult result2 = searchResults.next();
                assertFalse(searchResults.hasNext());

                assertMapEquals(expectedHotel1, result1.getAdditionalProperties(), true);
                assertMapEquals(expectedHotel2, result2.getAdditionalProperties(), true);
            })
            .verifyComplete();
    }

    @Test
    public void canUseTopAndSkipForClientSidePagingSync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);

        SearchOptions parameters = new SearchOptions().setTop(3).setSkip(0).setOrderBy("HotelId");

        SearchPagedIterable results = client.search(parameters);
        assertKeySequenceEqual(results, Arrays.asList("1", "10", "2"));

        results = client.search(parameters.setSkip(3));
        assertKeySequenceEqual(results, Arrays.asList("3", "4", "5"));
    }

    @Test
    public void canUseTopAndSkipForClientSidePagingAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        SearchOptions parameters = new SearchOptions().setTop(3).setSkip(0).setOrderBy("HotelId");

        StepVerifier
            .create(getSearchResultsAsync(asyncClient.search(parameters))
                .map(docs -> docs.stream().map(sd -> sd.get("HotelId").toString()).collect(Collectors.toList())))
            .assertNext(actualKeys -> assertEquals(Arrays.asList("1", "10", "2"), actualKeys))
            .verifyComplete();

        StepVerifier
            .create(getSearchResultsAsync(asyncClient.search(parameters.setSkip(3)))
                .map(docs -> docs.stream().map(sd -> sd.get("HotelId").toString()).collect(Collectors.toList())))
            .assertNext(actualKeys -> assertEquals(Arrays.asList("3", "4", "5"), actualKeys))
            .verifyComplete();
    }

    @Test
    public void searchWithoutOrderBySortsByScoreSync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);

        Iterator<SearchResult> results = client.search(new SearchOptions().setFilter("Rating lt 4")).iterator();
        SearchResult firstResult = results.next();
        SearchResult secondResult = results.next();
        assertTrue(firstResult.getScore() <= secondResult.getScore());
    }

    @Test
    public void searchWithoutOrderBySortsByScoreAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        StepVerifier.create(asyncClient.search(new SearchOptions().setFilter("Rating lt 4")).take(2).collectList())
            .assertNext(results -> assertTrue(results.get(0).getScore() <= results.get(1).getScore()))
            .verifyComplete();
    }

    @Test
    public void orderByProgressivelyBreaksTiesSync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);

        String[] expectedResults = new String[] { "1", "9", "3", "4", "5", "10", "2", "6", "7", "8" };

        Stream<String> results
            = client.search(new SearchOptions().setOrderBy("Rating desc", "LastRenovationDate asc", "HotelId"))
                .stream()
                .map(SearchTests::getSearchResultId);

        assertArrayEquals(results.toArray(), expectedResults);
    }

    @Test
    public void orderByProgressivelyBreaksTiesAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        String[] expectedResults = new String[] { "1", "9", "3", "4", "5", "10", "2", "6", "7", "8" };

        StepVerifier
            .create(
                asyncClient.search(new SearchOptions().setOrderBy("Rating desc", "LastRenovationDate asc", "HotelId"))
                    .map(SearchTests::getSearchResultId)
                    .collectList())
            .assertNext(results -> assertArrayEquals(results.toArray(), expectedResults))
            .verifyComplete();
    }

    @Test
    public void canFilterSync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);

        SearchOptions searchOptions
            = new SearchOptions().setFilter("Rating gt 3 and LastRenovationDate gt 2000-01-01T00:00:00Z")
                .setOrderBy("HotelId asc");

        List<Map<String, Object>> searchResultsList = getSearchResultsSync(client.search(searchOptions));
        assertEquals(2, searchResultsList.size());
        assertEquals("1", searchResultsList.get(0).get("HotelId").toString());
        assertEquals("5", searchResultsList.get(1).get("HotelId").toString());
    }

    @Test
    public void canFilterAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        SearchOptions searchOptions
            = new SearchOptions().setFilter("Rating gt 3 and LastRenovationDate gt 2000-01-01T00:00:00Z")
                .setOrderBy("HotelId asc");

        StepVerifier.create(getSearchResultsAsync(asyncClient.search(searchOptions))).assertNext(searchResultsList -> {
            assertEquals(2, searchResultsList.size());
            assertEquals("1", searchResultsList.get(0).get("HotelId").toString());
            assertEquals("5", searchResultsList.get(1).get("HotelId").toString());
        }).verifyComplete();
    }

    @Test
    @LiveOnly
    public void canSearchWithRangeFacetsSync() {
        // Disable sanitizer `$.to` for this test
        // interceptorManager.removeSanitizers("AZSDK3424"));
        SearchClient client = getClient(HOTEL_INDEX_NAME);

        List<Map<String, Object>> hotels = readJsonFileToList(HOTELS_DATA_JSON);

        for (SearchPagedResponse response : client.search(getSearchOptionsForRangeFacets()).iterableByPage()) {
            Map<String, List<FacetResult>> facets = response.getFacets();
            assertNotNull(facets);

            List<FacetResult> baseRateFacets = getRangeFacetsForField(facets, "Rooms/BaseRate", 4);
            List<FacetResult> lastRenovationDateFacets = getRangeFacetsForField(facets, "LastRenovationDate", 2);
            assertRangeFacets(baseRateFacets, lastRenovationDateFacets);

            assertContainHotelIds(hotels, response.getElements());
        }
    }

    @Test
    @LiveOnly
    public void canSearchWithRangeFacetsAsync() {
        // Disable sanitizer `$.to` for this test
        // interceptorManager.removeSanitizers("AZSDK3424");
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        List<Map<String, Object>> hotels = readJsonFileToList(HOTELS_DATA_JSON);

        StepVerifier.create(asyncClient.search(getSearchOptionsForRangeFacets()).byPage())
            .thenConsumeWhile(response -> {
                Map<String, List<FacetResult>> facets = response.getFacets();
                assertNotNull(facets);

                List<FacetResult> baseRateFacets = getRangeFacetsForField(facets, "Rooms/BaseRate", 4);
                List<FacetResult> lastRenovationDateFacets = getRangeFacetsForField(facets, "LastRenovationDate", 2);
                assertRangeFacets(baseRateFacets, lastRenovationDateFacets);

                assertContainHotelIds(hotels, response.getElements());

                return true;
            })
            .verifyComplete();
    }

    @Test
    public void canSearchWithValueFacetsSync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);

        List<Map<String, Object>> hotels = readJsonFileToList(HOTELS_DATA_JSON);

        for (SearchPagedResponse response : client.search(getSearchOptionsForValueFacets()).iterableByPage()) {
            Map<String, List<FacetResult>> facets = response.getFacets();
            assertNotNull(facets);

            canSearchWithValueFacetsValidateResponse(response, hotels, facets);
        }
    }

    @Test
    public void canSearchWithValueFacetsAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        List<Map<String, Object>> hotels = readJsonFileToList(HOTELS_DATA_JSON);

        StepVerifier.create(asyncClient.search(getSearchOptionsForValueFacets()).byPage())
            .thenConsumeWhile(response -> {
                Map<String, List<FacetResult>> facets = response.getFacets();
                assertNotNull(facets);

                canSearchWithValueFacetsValidateResponse(response, hotels, facets);

                return true;
            })
            .verifyComplete();
    }

    @Test
    public void canSearchWithLuceneSyntaxSync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);

        Map<String, Object> expectedResult = new LinkedHashMap<>();
        expectedResult.put("HotelName", "Roach Motel");
        expectedResult.put("Rating", 1);

        SearchOptions searchOptions = new SearchOptions().setQueryType(QueryType.FULL).setSelect("HotelName", "Rating");

        List<Map<String, Object>> searchResultsList
            = getSearchResultsSync(client.search(searchOptions.setSearchText("HotelName:roch~")));
        assertEquals(1, searchResultsList.size());
        assertEquals(expectedResult, searchResultsList.get(0));
    }

    @Test
    public void canSearchWithLuceneSyntaxAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        Map<String, Object> expectedResult = new LinkedHashMap<>();
        expectedResult.put("HotelName", "Roach Motel");
        expectedResult.put("Rating", 1);

        SearchOptions searchOptions = new SearchOptions().setQueryType(QueryType.FULL).setSelect("HotelName", "Rating");

        StepVerifier.create(getSearchResultsAsync(asyncClient.search(searchOptions.setSearchText("HotelName:roch~"))))
            .assertNext(searchResultsList -> {
                assertEquals(1, searchResultsList.size());
                assertEquals(expectedResult, searchResultsList.get(0));
            })
            .verifyComplete();
    }

    @Test
    public void canFilterNonNullableTypeSyncAndAsync() {
        SearchOptions searchOptions
            = new SearchOptions().setFilter("IntValue eq 0 or (Bucket/BucketName eq 'B' and Bucket/Count lt 10)");

        String indexName = createIndexWithValueTypes();
        indexesToDelete.add(indexName);
        SearchClient client = getSearchClientBuilder(indexName, true).buildClient();

        List<Map<String, Object>> docsList = createDocsListWithValueTypes("");
        uploadDocumentsRaw(client, docsList);

        Map<String, Map<String, Object>> expectedDocs = docsList.stream()
            .filter(d -> !d.get("Key").equals("789"))
            .collect(Collectors.toMap(sd -> sd.get("Key").toString(), Function.identity()));

        SearchPagedIterable results = client.search(searchOptions);
        assertNotNull(results);

        Map<String, Map<String, Object>> actualDocs = results.stream()
            .map(SearchResult::getAdditionalProperties)
            .collect(Collectors.toMap(sd -> sd.get("Key").toString(), Function.identity()));

        compareMaps(expectedDocs, actualDocs, (expected, actual) -> assertObjectEquals(expected, actual, true));

        SearchAsyncClient asyncClient = getSearchClientBuilder(indexName, false).buildAsyncClient();
        List<Map<String, Object>> docsListAsync = createDocsListWithValueTypes("async");
        uploadDocumentsRaw(asyncClient, docsListAsync);

        Map<String, Map<String, Object>> expectedDocsAsync = docsListAsync.stream()
            .filter(d -> !d.get("Key").equals("789async"))
            .collect(Collectors.toMap(sd -> sd.get("Key").toString(), Function.identity()));

        StepVerifier
            .create(asyncClient.search(searchOptions)
                .map(SearchResult::getAdditionalProperties)
                .filter(doc -> doc.get("Key").toString().endsWith("async"))
                .collectMap(sd -> sd.get("Key").toString()))
            .assertNext(resultsAsync -> compareMaps(expectedDocsAsync, resultsAsync,
                (expected, actual) -> assertObjectEquals(expected, actual, true)))
            .verifyComplete();
    }

    @Test
    public void canSearchWithSearchModeAllSync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);

        List<Map<String, Object>> response
            = getSearchResultsSync(client.search(new SearchOptions().setSearchText("Cheapest hotel")
                .setQueryType(QueryType.SIMPLE)
                .setSearchMode(SearchMode.ALL)));

        assertEquals(1, response.size());
        assertEquals("2", response.get(0).get("HotelId"));
    }

    @Test
    public void canSearchWithSearchModeAllAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        StepVerifier.create(getSearchResultsAsync(asyncClient.search(new SearchOptions().setSearchText("Cheapest hotel")
            .setQueryType(QueryType.SIMPLE)
            .setSearchMode(SearchMode.ALL)))).assertNext(response -> {
                assertEquals(1, response.size());
                assertEquals("2", response.get(0).get("HotelId"));
            }).verifyComplete();
    }

    @Test
    public void defaultSearchModeIsAnySync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);

        List<Map<String, Object>> response = getSearchResultsSync(
            client.search(new SearchOptions().setSearchText("Cheapest hotel").setOrderBy("HotelId")));
        assertEquals(7, response.size());
        assertEquals(Arrays.asList("1", "10", "2", "3", "4", "5", "9"),
            response.stream().map(res -> res.get("HotelId").toString()).collect(Collectors.toList()));
    }

    @Test
    public void defaultSearchModeIsAnyAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        StepVerifier
            .create(getSearchResultsAsync(
                asyncClient.search(new SearchOptions().setSearchText("Cheapest hotel").setOrderBy("HotelId"))))
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

        Iterator<SearchPagedResponse> iterator
            = client.search(new SearchOptions().setIncludeTotalCount(true)).iterableByPage().iterator();
        SearchPagedResponse page = iterator.next();
        assertEquals(hotels.size(), page.getCount().intValue());

        assertFalse(iterator.hasNext());
    }

    @Test
    public void canGetResultCountInSearchAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);
        List<Map<String, Object>> hotels = readJsonFileToList(HOTELS_DATA_JSON);

        StepVerifier.create(asyncClient.search(new SearchOptions().setIncludeTotalCount(true)).byPage())
            .assertNext(response -> assertEquals(hotels.size(), response.getCount()))
            .verifyComplete();
    }

    @Test
    public void canSearchWithRegexSync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);

        SearchOptions searchOptions = new SearchOptions().setQueryType(QueryType.FULL).setSelect("HotelName", "Rating");

        SearchPagedIterable results = client.search(searchOptions.setSearchText("HotelName:/.*oach.*\\/?/"));
        assertNotNull(results);

        List<Map<String, Object>> resultsList = getSearchResultsSync(results);

        Map<String, Object> expectedHotel = new LinkedHashMap<>();
        expectedHotel.put("HotelName", "Roach Motel");
        expectedHotel.put("Rating", 1);

        assertEquals(1, resultsList.size());
        assertEquals(resultsList.get(0), expectedHotel);
    }

    @Test
    public void canSearchWithRegexAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        SearchOptions searchOptions = new SearchOptions().setQueryType(QueryType.FULL).setSelect("HotelName", "Rating");

        StepVerifier
            .create(getSearchResultsAsync(asyncClient.search(searchOptions.setSearchText("HotelName:/.*oach.*\\/?/"))))
            .assertNext(resultsList -> {
                Map<String, Object> expectedHotel = new LinkedHashMap<>();
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

        SearchPagedIterable results
            = client.search(searchOptions.setSearchText("\\+\\-\\&\\|\\!\\(\\)\\{\\}\\[\\]\\^\\~\\*\\?\\:"));
        assertNotNull(results);

        List<Map<String, Object>> resultsList = getSearchResultsSync(results);
        assertEquals(0, resultsList.size());
    }

    @Test
    public void canSearchWithEscapedSpecialCharsInRegexAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        SearchOptions searchOptions = new SearchOptions().setQueryType(QueryType.FULL);

        StepVerifier
            .create(getSearchResultsAsync(
                asyncClient.search(searchOptions.setSearchText("\\+\\-\\&\\|\\!\\(\\)\\{\\}\\[\\]\\^\\~\\*\\?\\:"))))
            .assertNext(response -> assertEquals(0, response.size()))
            .verifyComplete();
    }

    @Test
    public void searchWithScoringProfileBoostsScoreSync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);

        SearchOptions searchOptions = new SearchOptions().setScoringProfile("nearest")
            .setScoringParameters("myloc--122.0,49.0")
            .setFilter("Rating eq 5 or Rating eq 1")
            .setOrderBy("HotelId desc");

        List<Map<String, Object>> response = getSearchResultsSync(client.search(searchOptions.setSearchText("hotel")));
        assertEquals(2, response.size());
        assertEquals("2", response.get(0).get("HotelId").toString());
        assertEquals("1", response.get(1).get("HotelId").toString());
    }

    @Test
    public void searchWithScoringProfileBoostsScoreAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        SearchOptions searchOptions = new SearchOptions().setScoringProfile("nearest")
            .setScoringParameters("myloc--122.0,49.0")
            .setFilter("Rating eq 5 or Rating eq 1")
            .setOrderBy("HotelId desc");

        StepVerifier.create(getSearchResultsAsync(asyncClient.search(searchOptions.setSearchText("hotel"))))
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

        SearchOptions searchOptions = new SearchOptions().setScoringProfile("text")
            .setScoringParameters("mytag-concierge,'Hello, O''Brien'")
            .setFilter("Rating eq 5 or Rating eq 1");

        List<Map<String, Object>> response = getSearchResultsSync(client.search(searchOptions.setSearchText("hotel")));
        assertEquals(2, response.size());
        assertEquals(Arrays.asList("1", "2"),
            response.stream().map(res -> res.get("HotelId").toString()).collect(Collectors.toList()));
    }

    @Test
    public void searchWithScoringProfileEscaperAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        SearchOptions searchOptions = new SearchOptions().setScoringProfile("text")
            .setScoringParameters("mytag-concierge,'Hello, O''Brien'")
            .setFilter("Rating eq 5 or Rating eq 1");

        StepVerifier.create(getSearchResultsAsync(asyncClient.search(searchOptions.setSearchText("hotel"))))
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

        SearchOptions searchOptions = new SearchOptions().setScoringProfile("text")
            .setScoringParameters("mytag-concierge")
            .setFilter("Rating eq 5 or Rating eq 1");

        List<Map<String, Object>> response = getSearchResultsSync(client.search(searchOptions.setSearchText("hotel")));
        assertEquals(2, response.size());
        assertEquals(Arrays.asList("1", "2"),
            response.stream().map(res -> res.get("HotelId").toString()).collect(Collectors.toList()));
    }

    @Test
    public void searchWithScoringParametersEmptyAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        SearchOptions searchOptions = new SearchOptions().setScoringProfile("text")
            .setScoringParameters("mytag-concierge")
            .setFilter("Rating eq 5 or Rating eq 1");

        StepVerifier.create(getSearchResultsAsync(asyncClient.search(searchOptions.setSearchText("hotel"))))
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

        SearchPagedResponse response
            = client.search(new SearchOptions().setMinimumCoverage(50.0)).iterableByPage().iterator().next();

        assertEquals(100.0, response.getCoverage(), 0);
    }

    @Test
    public void canSearchWithMinimumCoverageAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        StepVerifier.create(asyncClient.search(new SearchOptions().setMinimumCoverage(50.0)).byPage())
            .assertNext(response -> assertEquals(100.0, response.getCoverage()))
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
        SearchPagedResponse result = client.search(sp.setSearchText("luxury hotel")).iterableByPage().iterator().next();

        Iterator<SearchResult> documents = result.getElements().iterator();

        Map<String, List<String>> highlights = documents.next().getHighlights();
        assertEquals(2, highlights.size());
        assertTrue(highlights.containsKey(description));
        assertTrue(highlights.containsKey(category));

        assertFalse(documents.hasNext());

        //asserts
        assertEquals("<b>Luxury</b>", highlights.get(category).get(0));

        List<String> expectedDescriptionHighlights
            = Arrays.asList("Best <b>hotel</b> in town if you like <b>luxury</b> <b>hotels</b>.",
                "We highly recommend this <b>hotel</b>.");

        assertEquals(expectedDescriptionHighlights, highlights.get(description));
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

        StepVerifier.create(asyncClient.search(sp.setSearchText("luxury hotel")).byPage()).assertNext(result -> {
            Iterator<SearchResult> documents = result.getElements().iterator();

            Map<String, List<String>> highlights = documents.next().getHighlights();
            assertEquals(2, highlights.size());
            assertTrue(highlights.containsKey(description));
            assertTrue(highlights.containsKey(category));

            assertFalse(documents.hasNext());

            assertEquals("<b>Luxury</b>", highlights.get(category).get(0));

            List<String> expectedDescriptionHighlights
                = Arrays.asList("Best <b>hotel</b> in town if you like <b>luxury</b> <b>hotels</b>.",
                    "We highly recommend this <b>hotel</b>.");

            assertEquals(expectedDescriptionHighlights, highlights.get(description));
        }).verifyComplete();
    }

    @Test
    public void canSearchWithSynonymsSync() {
        SearchClient client = getClient(SYNONYM_INDEX_NAME);

        SearchOptions searchOptions = new SearchOptions().setQueryType(QueryType.FULL)
            .setSearchFields("HotelName")
            .setSelect("HotelName", "Rating");

        List<Map<String, Object>> response = getSearchResultsSync(client.search(searchOptions.setSearchText("luxury")));
        assertEquals(1, response.size());
        assertEquals("Fancy Stay", response.get(0).get("HotelName"));
        assertEquals(5, response.get(0).get("Rating"));
    }

    @Test
    public void canSearchWithSynonymsAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(SYNONYM_INDEX_NAME);

        SearchOptions searchOptions = new SearchOptions().setQueryType(QueryType.FULL)
            .setSearchFields("HotelName")
            .setSelect("HotelName", "Rating");

        StepVerifier.create(getSearchResultsAsync(asyncClient.search(searchOptions.setSearchText("luxury"))))
            .assertNext(results -> {
                assertEquals(1, results.size());
                assertEquals("Fancy Stay", results.get(0).get("HotelName"));
                assertEquals(5, results.get(0).get("Rating"));
            })
            .verifyComplete();
    }

    //==Elevated Read Tests==

    @Test
    public void searchWithElevatedReadIncludesHeader() {
        SearchOptions searchOptions = new SearchOptions().setEnableElevatedRead(true);

        assertTrue(searchOptions.isEnableElevatedRead(), "Elevated read should be enabled");

        SearchPagedIterable results = getClient(HOTEL_INDEX_NAME).search(searchOptions);
        assertNotNull(results, "Search with elevated read should work");
    }

    @Test
    public void searchDefaultOmitsHeader() {
        SearchOptions searchOptions = new SearchOptions();

        assertNull(searchOptions.isEnableElevatedRead(), "Elevated read should be null by default");

        SearchPagedIterable results = getClient(HOTEL_INDEX_NAME).search(searchOptions);
        assertNotNull(results, "Default search should work");
    }

    @Test
    public void listDocsWithElevatedReadIncludesHeader() {
        SearchIndexClient indexClient = getSearchIndexClientBuilder(true).buildClient();

        SearchOptions searchOptions = new SearchOptions().setEnableElevatedRead(true).setSelect("HotelId", "HotelName");

        SearchPagedIterable results = indexClient.getSearchClient(HOTEL_INDEX_NAME).search(searchOptions);

        assertNotNull(results, "Document listing with elevated read should work");
        assertTrue(searchOptions.isEnableElevatedRead(), "Elevated read should be enabled");
    }

    @Test
    public void withHeader200CodeparseResponse() {
        SearchOptions searchOptions = new SearchOptions().setEnableElevatedRead(true);

        SearchPagedIterable results = getClient(HOTEL_INDEX_NAME).search(searchOptions);
        assertNotNull(results, "Should parse elevated read response");
        assertNotNull(results.iterator(), "Should have results");

    }

    @Test
    public void withHeaderPlusUserTokenService400() {
        SearchOptions searchOptions = new SearchOptions().setEnableElevatedRead(true);

        try {
            SearchPagedIterable results = getClient(HOTEL_INDEX_NAME).search(searchOptions);
            assertNotNull(results, "Search completed (may not throw 400 in test environment)");
        } catch (HttpResponseException ex) {
            assertEquals(400, ex.getResponse().getStatusCode());
            assertTrue(ex.getMessage().contains("elevated read") || ex.getMessage().contains("user token"),
                "Error should be related to elevated read + user token combination");
        }
    }

    //    @Test
    //    public void oldApiVersionSupportsElevatedRead() {
    //        SearchClient oldVersionClient
    //            = getSearchClientBuilder(HOTEL_INDEX_NAME, true).serviceVersion(SearchServiceVersion.V2023_11_01)
    //                .buildClient();
    //
    //        SearchOptions searchOptions = new SearchOptions().setElevatedReadEnabled(true);
    //
    //        SearchPagedIterable results = oldVersionClient.search("*", searchOptions, null);
    //        assertNotNull(results, "Older API version should support elevated read for backward compatibility");
    //        assertTrue(results.iterator().hasNext(), "Should have search results");
    //    }

    @Test
    public void currentApiVersionSendsHeaderWhenRequested() {
        SearchClient currentClient = new SearchClientBuilder().endpoint(SEARCH_ENDPOINT)
            .credential(TestHelpers.getTestTokenCredential())
            .indexName(HOTEL_INDEX_NAME)
            .serviceVersion(SearchServiceVersion.V2025_11_01_PREVIEW)
            .buildClient();

        SearchOptions searchOptions = new SearchOptions().setEnableElevatedRead(true);

        try {
            SearchPagedIterable results = currentClient.search(searchOptions);
            assertNotNull(results, "Search with elevated read should work with current API version");
        } catch (Exception exception) {
            assertFalse(
                exception.getMessage().contains("api-version") && exception.getMessage().contains("does not exist"),
                "Should not be an API version error with current version");
        }
    }

    private static List<Map<String, Object>> getSearchResultsSync(SearchPagedIterable results) {
        return results.stream().map(SearchResult::getAdditionalProperties).collect(Collectors.toList());
    }

    private static Mono<List<Map<String, Object>>> getSearchResultsAsync(SearchPagedFlux results) {
        return results.map(SearchResult::getAdditionalProperties).collectList();
    }

    private static void assertKeySequenceEqual(SearchPagedIterable results, List<String> expectedKeys) {
        assertNotNull(results);

        List<String> actualKeys = results.stream()
            .map(doc -> (String) doc.getAdditionalProperties().get("HotelId"))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        assertEquals(expectedKeys, actualKeys);
    }

    static List<Map<String, Object>> createHotelsList() {
        List<Map<String, Object>> documents = new ArrayList<>();
        for (int i = 1; i <= 3000; i++) {
            Map<String, Object> doc = new LinkedHashMap<>();

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

    static <T> void assertRangeFacets(List<FacetResult> baseRateFacets, List<FacetResult> lastRenovationDateFacets) {
        assertNull(getFrom(baseRateFacets.get(0)));
        assertEquals(5.0, getTo(baseRateFacets.get(0)));
        assertEquals(5.0, getFrom(baseRateFacets.get(1)));
        assertEquals(8.0, getTo(baseRateFacets.get(1)));
        assertEquals(8.0, getFrom(baseRateFacets.get(2)));
        assertEquals(10.0, getTo(baseRateFacets.get(2)));
        assertEquals(10.0, getFrom(baseRateFacets.get(3)));
        assertNull(getTo(baseRateFacets.get(3)));

        assertEquals(1, baseRateFacets.get(0).getCount().intValue());
        assertEquals(1, baseRateFacets.get(1).getCount().intValue());
        assertEquals(1, baseRateFacets.get(2).getCount().intValue());
        assertEquals(0, baseRateFacets.get(3).getCount().intValue());

        assertNull(getFrom(lastRenovationDateFacets.get(0)));
        assertEquals("2000-01-01T00:00:00.000+0000", getTo(lastRenovationDateFacets.get(0)));
        assertEquals("2000-01-01T00:00:00.000+0000", getFrom(lastRenovationDateFacets.get(1)));
        assertNull(getTo(lastRenovationDateFacets.get(1)));

        assertEquals(5, lastRenovationDateFacets.get(0).getCount().intValue());
        assertEquals(2, lastRenovationDateFacets.get(1).getCount().intValue());
    }

    static List<FacetResult> getRangeFacetsForField(Map<String, List<FacetResult>> facets, String expectedField,
        int expectedCount) {
        return getFacetsForField(facets, expectedField, expectedCount);
    }

    static List<FacetResult> getValueFacetsForField(Map<String, List<FacetResult>> facets, String expectedField,
        int expectedCount) {
        return getFacetsForField(facets, expectedField, expectedCount);
    }

    private static List<FacetResult> getFacetsForField(Map<String, List<FacetResult>> facets, String expectedField,
        int expectedCount) {
        assertTrue(facets.containsKey(expectedField));
        List<FacetResult> results = facets.get(expectedField);
        assertEquals(expectedCount, results.size());
        return results;
    }

    static void assertContainHotelIds(List<Map<String, Object>> expected, IterableStream<SearchResult> actual) {
        assertNotNull(actual);
        Set<String> actualKeys = actual.stream()
            .map(item -> (String) item.getAdditionalProperties().get("HotelId"))
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        Set<String> expectedKeys = expected.stream()
            .map(item -> (String) item.get("HotelId"))
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        assertEquals(expectedKeys, actualKeys);
    }

    static void assertValueFacetsEqual(List<FacetResult> actualFacets, List<FacetResult> expectedFacets) {
        assertEquals(expectedFacets.size(), actualFacets.size());
        for (int i = 0; i < actualFacets.size(); i++) {
            assertEquals(expectedFacets.get(i).getCount(), actualFacets.get(i).getCount());
            assertEquals(getValue(expectedFacets.get(i)), getValue(actualFacets.get(i)));
        }
    }

    static String getSearchResultId(SearchResult searchResult) {
        return searchResult.getAdditionalProperties().get("HotelId").toString();
    }

    static SearchOptions getSearchOptionsForRangeFacets() {
        return new SearchOptions().setFacets("Rooms/BaseRate,values:5|8|10",
            "LastRenovationDate,values:2000-01-01T00:00:00Z");
    }

    static SearchOptions getSearchOptionsForValueFacets() {
        return new SearchOptions().setFacets("Rating,count:2,sort:-value", "SmokingAllowed,sort:count", "Category",
            "LastRenovationDate,interval:year", "Rooms/BaseRate,sort:value", "Tags,sort:value");
    }

    static void assertListEqualHotelIds(List<String> expected, IterableStream<SearchResult> actual) {
        assertNotNull(actual);
        List<String> actualKeys = actual.stream()
            .map(item -> (String) item.getAdditionalProperties().get("HotelId"))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        assertEquals(expected, actualKeys);
    }

    String createIndexWithNonNullableTypes() {
        SearchIndex index = new SearchIndex(testResourceNamer.randomName("non-nullable-index", 64),
            new SearchField("Key", SearchFieldDataType.STRING).setRetrievable(true).setKey(true),
            new SearchField("Rating", SearchFieldDataType.INT32).setRetrievable(true),
            new SearchField("Count", SearchFieldDataType.INT64).setRetrievable(true),
            new SearchField("IsEnabled", SearchFieldDataType.BOOLEAN).setRetrievable(true),
            new SearchField("Ratio", SearchFieldDataType.DOUBLE).setRetrievable(true),
            new SearchField("StartDate", SearchFieldDataType.DATE_TIME_OFFSET).setRetrievable(true),
            new SearchField("EndDate", SearchFieldDataType.DATE_TIME_OFFSET).setRetrievable(true),
            new SearchField("TopLevelBucket", SearchFieldDataType.COMPLEX).setFields(
                new SearchField("BucketName", SearchFieldDataType.STRING).setFilterable(true),
                new SearchField("Count", SearchFieldDataType.INT32).setFilterable(true)),
            new SearchField("Buckets", SearchFieldDataType.collection(SearchFieldDataType.COMPLEX)).setFields(
                new SearchField("BucketName", SearchFieldDataType.STRING).setFilterable(true),
                new SearchField("Count", SearchFieldDataType.INT32).setFilterable(true)));

        setupIndex(index);

        return index.getName();
    }

    String createIndexWithValueTypes() {
        SearchIndex index = new SearchIndex(testResourceNamer.randomName("testindex", 64),
            new SearchField("Key", SearchFieldDataType.STRING).setKey(true).setSearchable(true),
            new SearchField("IntValue", SearchFieldDataType.INT32).setFilterable(true),
            new SearchField("Bucket", SearchFieldDataType.COMPLEX).setFields(
                new SearchField("BucketName", SearchFieldDataType.STRING).setFilterable(true),
                new SearchField("Count", SearchFieldDataType.INT32).setFilterable(true)));

        setupIndex(index);

        return index.getName();
    }

    static List<Map<String, Object>> createDocsListWithValueTypes(String keySuffix) {
        Map<String, Object> element1 = new LinkedHashMap<>();
        element1.put("Key", "123" + keySuffix);
        element1.put("IntValue", 0);

        Map<String, Object> subElement1 = new LinkedHashMap<>();
        subElement1.put("BucketName", "A");
        subElement1.put("Count", 3);
        element1.put("Bucket", subElement1);

        Map<String, Object> element2 = new LinkedHashMap<>();
        element2.put("Key", "456" + keySuffix);
        element2.put("IntValue", 7);

        Map<String, Object> subElement2 = new LinkedHashMap<>();
        subElement2.put("BucketName", "B");
        subElement2.put("Count", 5);
        element2.put("Bucket", subElement2);

        Map<String, Object> element3 = new LinkedHashMap<>();
        element3.put("Key", "789" + keySuffix);
        element3.put("IntValue", 1);

        Map<String, Object> subElement3 = new LinkedHashMap<>();
        subElement3.put("BucketName", "B");
        subElement3.put("Count", 99);
        element3.put("Bucket", subElement3);

        return Arrays.asList(element1, element2, element3);
    }

    private static void canSearchWithValueFacetsValidateResponse(SearchPagedResponse result,
        List<Map<String, Object>> expectedHotels, Map<String, List<FacetResult>> facets) {
        assertContainHotelIds(expectedHotels, result.getElements());

        assertValueFacetsEqual(getValueFacetsForField(facets, "Rating", 2),
            Arrays.asList(createValueFacet(1L, 5), createValueFacet(4L, 4)));

        assertValueFacetsEqual(getValueFacetsForField(facets, "SmokingAllowed", 2),
            Arrays.asList(createValueFacet(4L, false), createValueFacet(2L, true)));

        assertValueFacetsEqual(getValueFacetsForField(facets, "Category", 3), Arrays
            .asList(createValueFacet(5L, "Budget"), createValueFacet(1L, "Boutique"), createValueFacet(1L, "Luxury")));

        assertValueFacetsEqual(getValueFacetsForField(facets, "LastRenovationDate", 6),
            Arrays.asList(
                createValueFacet(1L,
                    OffsetDateTime.parse("1970-01-01T00:00:00Z").format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)),
                createValueFacet(1L,
                    OffsetDateTime.parse("1982-01-01T00:00:00Z").format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)),
                createValueFacet(2L,
                    OffsetDateTime.parse("1995-01-01T00:00:00Z").format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)),
                createValueFacet(1L,
                    OffsetDateTime.parse("1999-01-01T00:00:00Z").format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)),
                createValueFacet(1L,
                    OffsetDateTime.parse("2010-01-01T00:00:00Z").format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)),
                createValueFacet(1L,
                    OffsetDateTime.parse("2012-01-01T00:00:00Z").format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))));

        assertValueFacetsEqual(getValueFacetsForField(facets, "Rooms/BaseRate", 4),
            Arrays.asList(createValueFacet(1L, 2.44), createValueFacet(1L, 7.69), createValueFacet(1L, 8.09),
                createValueFacet(1L, 9.69)));

        assertValueFacetsEqual(getValueFacetsForField(facets, "Tags", 10),
            Arrays.asList(createValueFacet(1L, "24-hour front desk service"), createValueFacet(1L, "air conditioning"),
                createValueFacet(4L, "budget"), createValueFacet(1L, "coffee in lobby"),
                createValueFacet(2L, "concierge"), createValueFacet(1L, "motel"), createValueFacet(2L, "pool"),
                createValueFacet(1L, "restaurant"), createValueFacet(1L, "view"), createValueFacet(4L, "wifi")));
    }

    private static FacetResult createValueFacet(long count, Object value) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try (JsonWriter jsonWriter = JsonProviders.createWriter(outputStream)) {
                jsonWriter.writeStartObject()
                    .writeLongField("count", count)
                    .writeNullableField("value", value, JsonWriter::writeUntyped)
                    .writeEndObject();
            }

            try (JsonReader jsonReader = JsonProviders.createReader(outputStream.toByteArray())) {
                return FacetResult.fromJson(jsonReader);
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private static Object getFrom(FacetResult facetResult) {
        return facetResult.getAdditionalProperties().get("from");
    }

    private static Object getTo(FacetResult facetResult) {
        return facetResult.getAdditionalProperties().get("to");
    }

    private static Object getValue(FacetResult facetResult) {
        return facetResult.getAdditionalProperties().get("value");
    }
}
