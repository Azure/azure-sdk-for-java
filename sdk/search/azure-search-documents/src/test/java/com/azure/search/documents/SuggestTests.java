// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.models.IndexActionType;
import com.azure.search.documents.models.IndexDocumentsBatch;
import com.azure.search.documents.models.SuggestDocumentsResult;
import com.azure.search.documents.models.SuggestOptions;
import com.azure.search.documents.models.SuggestResult;
import com.azure.search.documents.test.environment.models.Author;
import com.azure.search.documents.test.environment.models.Book;
import com.azure.search.documents.test.environment.models.Hotel;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import reactor.test.StepVerifier;

import java.net.HttpURLConnection;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.azure.search.documents.TestHelpers.convertFromMapStringObject;
import static com.azure.search.documents.TestHelpers.convertToMapStringObject;
import static com.azure.search.documents.TestHelpers.createIndexAction;
import static com.azure.search.documents.TestHelpers.readJsonFileToList;
import static com.azure.search.documents.TestHelpers.setupSharedIndex;
import static com.azure.search.documents.TestHelpers.waitForIndexing;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Execution(ExecutionMode.CONCURRENT)
public class SuggestTests extends SearchTestBase {
    private static final String BOOKS_INDEX_JSON = "BooksIndexData.json";
    private static final String HOTEL_INDEX_NAME = "azsearch-suggest-shared-hotel-instance";
    private static final String BOOKS_INDEX_NAME = "azsearch-suggest-shared-books-instance";

    private static SearchIndexClient searchIndexClient;

    @BeforeAll
    public static void setupClass() {
        TestProxyTestBase.setupClass();

        if (TEST_MODE == TestMode.PLAYBACK) {
            return;
        }

        searchIndexClient = setupSharedIndex(HOTEL_INDEX_NAME, HOTELS_TESTS_INDEX_DATA_JSON, HOTELS_DATA_JSON);
        setupSharedIndex(BOOKS_INDEX_NAME, BOOKS_INDEX_JSON, null);

        Author tolkien = new Author();
        tolkien.firstName("J.R.R.");
        tolkien.lastName("Tolkien");
        Book doc1 = new Book();
        doc1.ISBN("123");
        doc1.title("Lord of the Rings");
        doc1.author(tolkien);

        Book doc2 = new Book();
        doc2.ISBN("456");
        doc2.title("War and Peace");
        doc2.publishDate(OffsetDateTime.parse("2015-08-18T00:00:00Z"));

        searchIndexClient.getSearchClient(BOOKS_INDEX_NAME)
            .indexDocuments(
                new IndexDocumentsBatch(createIndexAction(IndexActionType.UPLOAD, convertToMapStringObject(doc1)),
                    createIndexAction(IndexActionType.UPLOAD, convertToMapStringObject(doc2))));
        waitForIndexing();
    }

    @AfterAll
    protected static void cleanupClass() {
        if (TEST_MODE != TestMode.PLAYBACK) {
            searchIndexClient.deleteIndex(HOTEL_INDEX_NAME);
            searchIndexClient.deleteIndex(BOOKS_INDEX_NAME);
        }
    }

    private SearchClient getClient(String indexName) {
        return getSearchClientBuilder(indexName, true).buildClient();
    }

    private SearchAsyncClient getAsyncClient(String indexName) {
        return getSearchClientBuilder(indexName, false).buildAsyncClient();
    }

    @Test
    public void canSuggestDynamicDocumentsSync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);
        SuggestOptions suggestOptions = new SuggestOptions("more", "sg").setOrderBy("HotelId");

        verifyDynamicDocumentSuggest(client.suggest(suggestOptions));
    }

    @Test
    public void canSuggestDynamicDocumentsAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);
        SuggestOptions suggestOptions = new SuggestOptions("more", "sg").setOrderBy("HotelId");

        StepVerifier.create(asyncClient.suggest(suggestOptions))
            .assertNext(SuggestTests::verifyDynamicDocumentSuggest)
            .verifyComplete();
    }

    @Test
    public void searchFieldsExcludesFieldsFromSuggestSync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);
        SuggestOptions suggestOptions = new SuggestOptions("luxury", "sg").setSearchFields("HotelName");

        verifySuggestionCount(client.suggest(suggestOptions), 0);
    }

    @Test
    public void searchFieldsExcludesFieldsFromSuggestAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);
        SuggestOptions suggestOptions = new SuggestOptions("luxury", "sg").setSearchFields("HotelName");

        StepVerifier.create(asyncClient.suggest(suggestOptions))
            .assertNext(response -> verifySuggestionCount(response, 0))
            .verifyComplete();
    }

    @Test
    public void canUseSuggestHitHighlightingSync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);
        SuggestOptions suggestOptions = new SuggestOptions("hotel", "sg").setHighlightPreTag("<b>")
            .setHighlightPostTag("</b>")
            .setFilter("Category eq 'Luxury'")
            .setTop(1);

        verifyHitHighlightingSuggest(client.suggest(suggestOptions));
    }

    @Test
    public void canUseSuggestHitHighlightingAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);
        SuggestOptions suggestOptions = new SuggestOptions("hotel", "sg").setHighlightPreTag("<b>")
            .setHighlightPostTag("</b>")
            .setFilter("Category eq 'Luxury'")
            .setTop(1);

        StepVerifier.create(asyncClient.suggest(suggestOptions))
            .assertNext(SuggestTests::verifyHitHighlightingSuggest)
            .verifyComplete();
    }

    @Test
    public void canGetFuzzySuggestionsSync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);
        SuggestOptions suggestOptions = new SuggestOptions("hitel", "sg").setUseFuzzyMatching(true);

        verifySuggestionCount(client.suggest(suggestOptions), 5);
    }

    @Test
    public void canGetFuzzySuggestionsAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);
        SuggestOptions suggestOptions = new SuggestOptions("hitel", "sg").setUseFuzzyMatching(true);

        StepVerifier.create(asyncClient.suggest(suggestOptions))
            .assertNext(response -> verifySuggestionCount(response, 5))
            .verifyComplete();
    }

    @Test
    public void canSuggestStaticallyTypedDocumentsSync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);
        List<Map<String, Object>> hotels = readJsonFileToList(HOTELS_DATA_JSON);
        SuggestOptions suggestOptions = new SuggestOptions("more", "sg").setOrderBy("HotelId");

        verifyCanSuggestStaticallyTypedDocuments(client.suggest(suggestOptions), hotels);
    }

    @Test
    public void canSuggestStaticallyTypedDocumentsAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);
        List<Map<String, Object>> hotels = readJsonFileToList(HOTELS_DATA_JSON);
        SuggestOptions suggestOptions = new SuggestOptions("more", "sg").setOrderBy("HotelId");

        StepVerifier.create(asyncClient.suggest(suggestOptions))
            .assertNext(response -> verifyCanSuggestStaticallyTypedDocuments(response, hotels))
            .verifyComplete();
    }

    @Test
    public void canSuggestWithDateTimeInStaticModelSync() {
        SearchClient client = getClient(BOOKS_INDEX_NAME);
        SuggestOptions suggestOptions = new SuggestOptions("War", "sg").setSelect("ISBN", "Title", "PublishDate");

        verifyCanSuggestWithDateTimeInStaticModel(client.suggest(suggestOptions));
    }

    @Test
    public void canSuggestWithDateTimeInStaticModelAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(BOOKS_INDEX_NAME);
        SuggestOptions suggestOptions = new SuggestOptions("War", "sg").setSelect("ISBN", "Title", "PublishDate");

        StepVerifier.create(asyncClient.suggest(suggestOptions))
            .assertNext(SuggestTests::verifyCanSuggestWithDateTimeInStaticModel)
            .verifyComplete();
    }

    @Test
    public void fuzzyIsOffByDefaultSync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);

        verifySuggestionCount(client.suggest(new SuggestOptions("hitel", "sg")), 0);
    }

    @Test
    public void fuzzyIsOffByDefaultAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        StepVerifier.create(asyncClient.suggest(new SuggestOptions("hitel", "sg")))
            .assertNext(response -> verifySuggestionCount(response, 0))
            .verifyComplete();
    }

    @Test
    public void suggestThrowsWhenGivenBadSuggesterNameSync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);

        HttpResponseException ex = assertThrows(HttpResponseException.class,
            () -> client.suggest(new SuggestOptions("Hotel", "Suggester does not exist")));
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, ex.getResponse().getStatusCode());
    }

    @Test
    public void suggestThrowsWhenGivenBadSuggesterNameAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        StepVerifier.create(asyncClient.suggest(new SuggestOptions("Hotel", "Suggester does not exist")))
            .verifyErrorSatisfies(throwable -> {
                HttpResponseException ex = assertInstanceOf(HttpResponseException.class, throwable);
                assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, ex.getResponse().getStatusCode());
            });
    }

    @Test
    public void suggestThrowsWhenRequestIsMalformedSync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);
        SuggestOptions suggestOptions = new SuggestOptions("hotel", "sg").setOrderBy("This is not a valid orderby.");

        HttpResponseException ex = assertThrows(HttpResponseException.class, () -> client.suggest(suggestOptions));
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, ex.getResponse().getStatusCode());
    }

    @Test
    public void suggestThrowsWhenRequestIsMalformedAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);
        SuggestOptions suggestOptions = new SuggestOptions("hotel", "sg").setOrderBy("This is not a valid orderby.");

        StepVerifier.create(asyncClient.suggest(suggestOptions)).verifyErrorSatisfies(throwable -> {
            HttpResponseException ex = assertInstanceOf(HttpResponseException.class, throwable);
            assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, ex.getResponse().getStatusCode());
        });
    }

    @Test
    public void testCanSuggestWithMinimumCoverageSync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);
        SuggestOptions suggestOptions
            = new SuggestOptions("luxury", "sg").setOrderBy("HotelId").setMinimumCoverage(50.0);

        verifyMinimumCoverage(client.suggest(suggestOptions));
    }

    @Test
    public void testCanSuggestWithMinimumCoverageAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);
        SuggestOptions suggestOptions
            = new SuggestOptions("luxury", "sg").setOrderBy("HotelId").setMinimumCoverage(50.0);

        StepVerifier.create(asyncClient.suggest(suggestOptions))
            .assertNext(SuggestTests::verifyMinimumCoverage)
            .verifyComplete();
    }

    @Test
    public void testTopTrimsResultsSync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);
        SuggestOptions suggestOptions = new SuggestOptions("hotel", "sg").setOrderBy("HotelId").setTop(3);

        verifyTopDocumentSuggest(client.suggest(suggestOptions));
    }

    @Test
    public void testTopTrimsResultsAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);
        SuggestOptions suggestOptions = new SuggestOptions("hotel", "sg").setOrderBy("HotelId").setTop(3);

        StepVerifier.create(asyncClient.suggest(suggestOptions))
            .assertNext(SuggestTests::verifyTopDocumentSuggest)
            .verifyComplete();
    }

    @Test
    public void testCanFilterSync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);
        SuggestOptions suggestOptions
            = new SuggestOptions("hotel", "sg").setFilter("Rating gt 3 and LastRenovationDate gt 2000-01-01T00:00:00Z")
                .setOrderBy("HotelId");

        List<String> actualIds = client.suggest(suggestOptions)
            .getResults()
            .stream()
            .map(s -> (String) s.getAdditionalProperties().get("HotelId"))
            .collect(Collectors.toList());
        assertEquals(Arrays.asList("1", "5"), actualIds);
    }

    @Test
    public void testCanFilterAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);
        SuggestOptions suggestOptions
            = new SuggestOptions("hotel", "sg").setFilter("Rating gt 3 and LastRenovationDate gt 2000-01-01T00:00:00Z")
                .setOrderBy("HotelId");

        StepVerifier.create(asyncClient.suggest(suggestOptions)).assertNext(response -> {
            List<String> actualIds = response.getResults()
                .stream()
                .map(sr -> sr.getAdditionalProperties().get("HotelId").toString())
                .collect(Collectors.toList());
            assertEquals(Arrays.asList("1", "5"), actualIds);
        }).verifyComplete();
    }

    @Test
    public void testOrderByProgressivelyBreaksTiesSync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);
        SuggestOptions suggestOptions = new SuggestOptions("hotel", "sg").setOrderBy("Rating desc",
            "LastRenovationDate asc", "geo.distance(Location, geography'POINT(-122.0 49.0)')");

        List<String> actualIds = client.suggest(suggestOptions)
            .getResults()
            .stream()
            .map(s -> (String) s.getAdditionalProperties().get("HotelId"))
            .collect(Collectors.toList());
        assertEquals(Arrays.asList("1", "9", "4", "3", "5"), actualIds);
    }

    @Test
    public void testOrderByProgressivelyBreaksTiesAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);
        SuggestOptions suggestOptions = new SuggestOptions("hotel", "sg").setOrderBy("Rating desc",
            "LastRenovationDate asc", "geo.distance(Location, geography'POINT(-122.0 49.0)')");

        StepVerifier.create(asyncClient.suggest(suggestOptions)).assertNext(response -> {
            List<String> actualIds = response.getResults()
                .stream()
                .map(sr -> sr.getAdditionalProperties().get("HotelId").toString())
                .collect(Collectors.toList());
            assertEquals(Arrays.asList("1", "9", "4", "3", "5"), actualIds);
        }).verifyComplete();
    }

    @Test
    public void testCanSuggestWithSelectedFieldsSync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);
        SuggestOptions suggestOptions
            = new SuggestOptions("secret", "sg").setSelect("HotelName", "Rating", "Address/City", "Rooms/Type");

        verifySuggestWithSelectedFields(client.suggest(suggestOptions));
    }

    @Test
    public void testCanSuggestWithSelectedFieldsAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        SuggestOptions suggestOptions
            = new SuggestOptions("secret", "sg").setSelect("HotelName", "Rating", "Address/City", "Rooms/Type");

        StepVerifier.create(asyncClient.suggest(suggestOptions))
            .assertNext(SuggestTests::verifySuggestWithSelectedFields)
            .verifyComplete();
    }

    static void verifySuggestionCount(SuggestDocumentsResult response, int count) {
        assertNotNull(response);
        assertEquals(count, response.getResults().size());
    }

    static void verifyHitHighlightingSuggest(SuggestDocumentsResult suggestResultPagedResponse) {
        assertNotNull(suggestResultPagedResponse);
        assertEquals(1, suggestResultPagedResponse.getResults().size());
        assertTrue(suggestResultPagedResponse.getResults().get(0).getText().startsWith("Best <b>hotel</b> in town"));
    }

    static void verifyDynamicDocumentSuggest(SuggestDocumentsResult suggestResultPagedResponse) {
        assertNotNull(suggestResultPagedResponse);
        assertEquals(2, suggestResultPagedResponse.getResults().size());
        Hotel hotel = convertFromMapStringObject(
            suggestResultPagedResponse.getResults().get(0).getAdditionalProperties(), Hotel::fromJson);
        assertEquals("10", hotel.hotelId());
    }

    static void verifyCanSuggestStaticallyTypedDocuments(SuggestDocumentsResult suggestResultPagedResponse,
        List<Map<String, Object>> expectedHotels) {
        //sanity
        assertNotNull(suggestResultPagedResponse);
        List<Map<String, Object>> docs = suggestResultPagedResponse.getResults()
            .stream()
            .map(SuggestResult::getAdditionalProperties)
            .collect(Collectors.toList());
        List<SuggestResult> hotelsList = suggestResultPagedResponse.getResults();

        List<Map<String, Object>> expectedHotelsList = expectedHotels.stream()
            .filter(h -> "10".equals(h.get("HotelId")) || "8".equals(h.get("HotelId")))
            .sorted(Comparator.comparing(h -> h.get("HotelId").toString()))
            .collect(Collectors.toList());

        //assert
        //verify fields
        assertEquals(2, docs.size());
        assertEquals(hotelsList.stream().map(SuggestResult::getText).collect(Collectors.toList()),
            expectedHotelsList.stream().map(hotel -> hotel.get("Description")).collect(Collectors.toList()));
    }

    static void verifyMinimumCoverage(SuggestDocumentsResult suggestResultPagedResponse) {
        assertNotNull(suggestResultPagedResponse);
        assertEquals(100.0D, suggestResultPagedResponse.getCoverage());
    }

    static void verifyTopDocumentSuggest(SuggestDocumentsResult suggestResultPagedResponse) {
        assertNotNull(suggestResultPagedResponse);
        assertEquals(3, suggestResultPagedResponse.getResults().size());
        List<String> resultIds = suggestResultPagedResponse.getResults()
            .stream()
            .map(hotel -> convertFromMapStringObject(hotel.getAdditionalProperties(), Hotel::fromJson).hotelId())
            .collect(Collectors.toList());

        assertEquals(Arrays.asList("1", "10", "2"), resultIds);
    }

    static void verifyCanSuggestWithDateTimeInStaticModel(SuggestDocumentsResult suggestResultPagedResponse) {
        List<SuggestResult> books = suggestResultPagedResponse.getResults();

        assertEquals(1, books.size());
        assertEquals("War and Peace", books.get(0).getText());
    }

    @SuppressWarnings("unchecked")
    static void verifySuggestWithSelectedFields(SuggestDocumentsResult suggestResultPagedResponse) {
        assertEquals(1, suggestResultPagedResponse.getResults().size());
        Map<String, Object> result = suggestResultPagedResponse.getResults().get(0).getAdditionalProperties();

        assertEquals("Secret Point Motel", result.get("HotelName"));
        assertEquals(4, ((Number) result.get("Rating")).intValue());
        assertEquals("New York", ((LinkedHashMap<?, ?>) result.get("Address")).get("City"));
        assertEquals(Arrays.asList("Budget Room", "Budget Room"),
            ((List<Map<String, String>>) result.get("Rooms")).stream()
                .map(room -> room.get("Type"))
                .collect(Collectors.toList()));
    }
}
