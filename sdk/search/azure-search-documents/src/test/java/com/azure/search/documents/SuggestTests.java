// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.PagedIterableBase;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.util.Context;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.models.SuggestOptions;
import com.azure.search.documents.models.SuggestResult;
import com.azure.search.documents.test.environment.models.Author;
import com.azure.search.documents.test.environment.models.Book;
import com.azure.search.documents.test.environment.models.Hotel;
import com.azure.search.documents.util.SuggestPagedIterable;
import com.azure.search.documents.util.SuggestPagedResponse;
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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.azure.search.documents.TestHelpers.readJsonFileToList;
import static com.azure.search.documents.TestHelpers.setupSharedIndex;
import static com.azure.search.documents.TestHelpers.waitForIndexing;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
        TestBase.setupClass();

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
            .uploadDocuments(Arrays.asList(doc1, doc2));
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

        SuggestOptions suggestOptions = new SuggestOptions().setOrderBy("HotelId");

        Iterator<SuggestPagedResponse> suggestResultIterator = client.suggest("more", "sg", suggestOptions,
                Context.NONE)
            .iterableByPage()
            .iterator();

        verifyDynamicDocumentSuggest(suggestResultIterator.next());
        assertFalse(suggestResultIterator.hasNext());
    }

    @Test
    public void canSuggestDynamicDocumentsAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        SuggestOptions suggestOptions = new SuggestOptions().setOrderBy("HotelId");

        StepVerifier.create(asyncClient.suggest("more", "sg", suggestOptions).byPage())
            .assertNext(SuggestTests::verifyDynamicDocumentSuggest)
            .verifyComplete();
    }

    @Test
    public void searchFieldsExcludesFieldsFromSuggestSync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);

        SuggestOptions suggestOptions = new SuggestOptions()
            .setSearchFields("HotelName");

        Iterator<SuggestPagedResponse> suggestResultIterator = client.suggest("luxury",
                "sg", suggestOptions, Context.NONE)
            .iterableByPage()
            .iterator();

        verifySuggestionCount(suggestResultIterator.next(), 0);
        assertFalse(suggestResultIterator.hasNext());
    }

    @Test
    public void searchFieldsExcludesFieldsFromSuggestAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        SuggestOptions suggestOptions = new SuggestOptions()
            .setSearchFields("HotelName");

        StepVerifier.create(asyncClient.suggest("luxury", "sg", suggestOptions).byPage())
            .assertNext(response -> verifySuggestionCount(response, 0))
            .verifyComplete();
    }

    @Test
    public void canUseSuggestHitHighlightingSync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);

        SuggestOptions suggestOptions = new SuggestOptions()
            .setHighlightPreTag("<b>")
            .setHighlightPostTag("</b>")
            .setFilter("Category eq 'Luxury'")
            .setTop(1);

        Iterator<SuggestPagedResponse> suggestResultIterator = client.suggest("hotel", "sg", suggestOptions,
                Context.NONE)
            .iterableByPage()
            .iterator();

        verifyHitHighlightingSuggest(suggestResultIterator.next());
        assertFalse(suggestResultIterator.hasNext());
    }

    @Test
    public void canUseSuggestHitHighlightingAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        SuggestOptions suggestOptions = new SuggestOptions()
            .setHighlightPreTag("<b>")
            .setHighlightPostTag("</b>")
            .setFilter("Category eq 'Luxury'")
            .setTop(1);

        StepVerifier.create(asyncClient.suggest("hotel", "sg", suggestOptions).byPage())
            .assertNext(SuggestTests::verifyHitHighlightingSuggest)
            .verifyComplete();
    }

    @Test
    public void canGetFuzzySuggestionsSync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);

        SuggestOptions suggestOptions = new SuggestOptions().setUseFuzzyMatching(true);

        Iterator<SuggestPagedResponse> suggestResultIterator = client.suggest("hitel", "sg", suggestOptions,
                Context.NONE)
            .iterableByPage()
            .iterator();

        verifySuggestionCount(suggestResultIterator.next(), 5);
        assertFalse(suggestResultIterator.hasNext());
    }

    @Test
    public void canGetFuzzySuggestionsAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        SuggestOptions suggestOptions = new SuggestOptions().setUseFuzzyMatching(true);

        StepVerifier.create(asyncClient.suggest("hitel", "sg", suggestOptions).byPage())
            .assertNext(response -> verifySuggestionCount(response, 5))
            .verifyComplete();
    }

    @Test
    public void canSuggestStaticallyTypedDocumentsSync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);

        List<Map<String, Object>> hotels = readJsonFileToList(HOTELS_DATA_JSON);
        //arrange
        SuggestOptions suggestOptions = new SuggestOptions()
            .setOrderBy("HotelId");

        //act
        Iterator<SuggestPagedResponse> suggestResultIterator = client.suggest("more", "sg", suggestOptions,
                Context.NONE)
            .iterableByPage()
            .iterator();

        //assert
        verifyCanSuggestStaticallyTypedDocuments(suggestResultIterator.next(), hotels);
    }

    @Test
    public void canSuggestStaticallyTypedDocumentsAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        List<Map<String, Object>> hotels = readJsonFileToList(HOTELS_DATA_JSON);
        //arrange
        SuggestOptions suggestOptions = new SuggestOptions().setOrderBy("HotelId");

        //act
        StepVerifier.create(asyncClient.suggest("more", "sg", suggestOptions).byPage())
            .assertNext(response -> verifyCanSuggestStaticallyTypedDocuments(response, hotels))
            .verifyComplete();
    }

    @Test
    public void canSuggestWithDateTimeInStaticModelSync() {
        SearchClient client = getClient(BOOKS_INDEX_NAME);

        SuggestOptions suggestOptions = new SuggestOptions();
        suggestOptions.setSelect("ISBN", "Title", "PublishDate");
        Iterator<SuggestPagedResponse> suggestResultIterator = client.suggest("War", "sg", suggestOptions, Context.NONE)
            .iterableByPage()
            .iterator();

        //assert
        verifyCanSuggestWithDateTimeInStaticModel(suggestResultIterator.next());
    }

    @Test
    public void canSuggestWithDateTimeInStaticModelAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(BOOKS_INDEX_NAME);

        SuggestOptions suggestOptions = new SuggestOptions();
        suggestOptions.setSelect("ISBN", "Title", "PublishDate");

        StepVerifier.create(asyncClient.suggest("War", "sg", suggestOptions).byPage())
            .assertNext(SuggestTests::verifyCanSuggestWithDateTimeInStaticModel)
            .verifyComplete();
    }

    @Test
    public void fuzzyIsOffByDefaultSync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);

        Iterator<SuggestPagedResponse> suggestResultIterator = client.suggest("hitel", "sg", null, Context.NONE)
            .iterableByPage()
            .iterator();

        verifySuggestionCount(suggestResultIterator.next(), 0);

        Iterator<SuggestPagedResponse> suggestResultWithoutSuggestOptions = client.suggest("hitel", "sg")
            .iterableByPage()
            .iterator();

        verifySuggestionCount(suggestResultWithoutSuggestOptions.next(), 0);
    }

    @Test
    public void fuzzyIsOffByDefaultAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        StepVerifier.create(asyncClient.suggest("hitel", "sg", null).byPage())
            .assertNext(response -> verifySuggestionCount(response, 0))
            .verifyComplete();

        StepVerifier.create(asyncClient.suggest("hitel", "sg").byPage())
            .assertNext(response -> verifySuggestionCount(response, 0))
            .verifyComplete();
    }

    @Test
    public void suggestThrowsWhenGivenBadSuggesterNameSync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);

        SuggestPagedIterable suggestResultIterator = client.suggest("Hotel", "Suggester does not exist",
            new SuggestOptions(), Context.NONE);

        HttpResponseException ex = assertThrows(HttpResponseException.class,
            () -> suggestResultIterator.iterableByPage().iterator().next());
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, ex.getResponse().getStatusCode());
    }

    @Test
    public void suggestThrowsWhenGivenBadSuggesterNameAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        StepVerifier.create(asyncClient.suggest("Hotel", "Suggester does not exist", new SuggestOptions()).byPage())
            .thenRequest(1)
            .verifyErrorSatisfies(throwable -> {
                HttpResponseException ex = assertInstanceOf(HttpResponseException.class, throwable);
                assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, ex.getResponse().getStatusCode());
            });
    }

    @Test
    public void suggestThrowsWhenRequestIsMalformedSync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);

        SuggestOptions suggestOptions = new SuggestOptions().setOrderBy("This is not a valid orderby.");

        SuggestPagedIterable suggestResultIterator = client.suggest("hotel", "sg", suggestOptions, Context.NONE);

        HttpResponseException ex = assertThrows(HttpResponseException.class,
            () -> suggestResultIterator.iterableByPage().iterator().next());
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, ex.getResponse().getStatusCode());
    }

    @Test
    public void suggestThrowsWhenRequestIsMalformedAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        SuggestOptions suggestOptions = new SuggestOptions().setOrderBy("This is not a valid orderby.");

        StepVerifier.create(asyncClient.suggest("hotel", "sg", suggestOptions))
            .thenRequest(1)
            .verifyErrorSatisfies(throwable -> {
                HttpResponseException ex = assertInstanceOf(HttpResponseException.class, throwable);
                assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, ex.getResponse().getStatusCode());
            });
    }

    @Test
    public void testCanSuggestWithMinimumCoverageSync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);

        //arrange
        SuggestOptions suggestOptions = new SuggestOptions()
            .setOrderBy("HotelId")
            .setMinimumCoverage(50.0);

        //act
        SuggestPagedResponse suggestPagedResponse = client.suggest("luxury", "sg", suggestOptions, Context.NONE)
            .iterableByPage()
            .iterator()
            .next();

        verifyMinimumCoverage(suggestPagedResponse);
    }

    @Test
    public void testCanSuggestWithMinimumCoverageAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        //arrange
        SuggestOptions suggestOptions = new SuggestOptions()
            .setOrderBy("HotelId")
            .setMinimumCoverage(50.0);

        //act
        StepVerifier.create(asyncClient.suggest("luxury", "sg", suggestOptions).byPage())
            .assertNext(SuggestTests::verifyMinimumCoverage)
            .verifyComplete();
    }

    @Test
    public void testTopTrimsResultsSync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);

        //arrange
        SuggestOptions suggestOptions = new SuggestOptions()
            .setOrderBy("HotelId")
            .setTop(3);

        //act
        SuggestPagedResponse suggestResultIterator = client.suggest("hotel", "sg", suggestOptions, Context.NONE)
            .iterableByPage()
            .iterator()
            .next();

        //assert
        verifyTopDocumentSuggest(suggestResultIterator);
    }

    @Test
    public void testTopTrimsResultsAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        //arrange
        SuggestOptions suggestOptions = new SuggestOptions()
            .setOrderBy("HotelId")
            .setTop(3);

        //act
        StepVerifier.create(asyncClient.suggest("hotel", "sg", suggestOptions).byPage())
            .assertNext(SuggestTests::verifyTopDocumentSuggest)
            .verifyComplete();
    }

    @Test
    public void testCanFilterSync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);

        SuggestOptions suggestOptions = new SuggestOptions()
            .setFilter("Rating gt 3 and LastRenovationDate gt 2000-01-01T00:00:00Z")
            .setOrderBy("HotelId");

        SuggestPagedResponse suggestPagedResponse = client.suggest("hotel", "sg", suggestOptions, Context.NONE)
            .iterableByPage()
            .iterator()
            .next();

        assertNotNull(suggestPagedResponse);
        List<String> actualIds = suggestPagedResponse.getValue().stream()
            .map(s -> (String) s.getDocument(SearchDocument.class).get("HotelId")).collect(Collectors.toList());
        List<String> expectedIds = Arrays.asList("1", "5");
        assertEquals(expectedIds, actualIds);
    }

    @Test
    public void testCanFilterAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        SuggestOptions suggestOptions = new SuggestOptions()
            .setFilter("Rating gt 3 and LastRenovationDate gt 2000-01-01T00:00:00Z")
            .setOrderBy("HotelId");

        StepVerifier.create(asyncClient.suggest("hotel", "sg", suggestOptions).byPage())
            .assertNext(response -> {
                List<String> expectedIds = Arrays.asList("1", "5");
                List<String> actualIds = response.getValue().stream()
                    .map(sr -> sr.getDocument(SearchDocument.class).get("HotelId").toString())
                    .collect(Collectors.toList());

                assertEquals(expectedIds, actualIds);
            })
            .verifyComplete();
    }

    @Test
    public void testOrderByProgressivelyBreaksTiesSync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);

        SuggestOptions suggestOptions = new SuggestOptions()
            .setOrderBy(
                "Rating desc",
                "LastRenovationDate asc",
                "geo.distance(Location, geography'POINT(-122.0 49.0)')");

        SuggestPagedResponse suggestPagedResponse = client.suggest("hotel", "sg", suggestOptions, Context.NONE)
            .iterableByPage().iterator().next();

        assertNotNull(suggestPagedResponse);
        List<String> actualIds = suggestPagedResponse.getValue().stream()
            .map(s -> (String) s.getDocument(SearchDocument.class).get("HotelId")).collect(Collectors.toList());
        List<String> expectedIds = Arrays.asList("1", "9", "4", "3", "5");
        assertEquals(expectedIds, actualIds);
    }

    @Test
    public void testOrderByProgressivelyBreaksTiesAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        SuggestOptions suggestOptions = new SuggestOptions()
            .setOrderBy(
                "Rating desc",
                "LastRenovationDate asc",
                "geo.distance(Location, geography'POINT(-122.0 49.0)')");

        StepVerifier.create(asyncClient.suggest("hotel", "sg", suggestOptions).byPage())
            .assertNext(response -> {
                List<String> expectedIds = Arrays.asList("1", "9", "4", "3", "5");
                List<String> actualIds = response.getValue().stream()
                    .map(sr -> sr.getDocument(SearchDocument.class).get("HotelId").toString())
                    .collect(Collectors.toList());

                assertEquals(expectedIds, actualIds);
            })
            .verifyComplete();
    }

    @Test
    public void testCanSuggestWithSelectedFieldsSync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);

        SuggestOptions suggestOptions = new SuggestOptions()
            .setSelect("HotelName", "Rating", "Address/City", "Rooms/Type");
        PagedIterableBase<SuggestResult, SuggestPagedResponse> suggestResult = client.suggest("secret", "sg",
            suggestOptions, Context.NONE);

        verifySuggestWithSelectedFields(suggestResult.iterableByPage().iterator().next());
    }

    @Test
    public void testCanSuggestWithSelectedFieldsAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        SuggestOptions suggestOptions = new SuggestOptions()
            .setSelect("HotelName", "Rating", "Address/City", "Rooms/Type");

        StepVerifier.create(asyncClient.suggest("secret", "sg", suggestOptions).byPage())
            .assertNext(SuggestTests::verifySuggestWithSelectedFields)
            .verifyComplete();
    }

    static void verifySuggestionCount(SuggestPagedResponse response, int count) {
        assertNotNull(response);
        assertEquals(count, response.getValue().size());
    }

    static void verifyHitHighlightingSuggest(SuggestPagedResponse suggestResultPagedResponse) {
        assertNotNull(suggestResultPagedResponse);
        assertEquals(1, suggestResultPagedResponse.getValue().size());
        assertTrue(
            suggestResultPagedResponse.getValue()
                .get(0)
                .getText()
                .startsWith("Best <b>hotel</b> in town"));
    }

    static void verifyDynamicDocumentSuggest(SuggestPagedResponse suggestResultPagedResponse) {
        assertNotNull(suggestResultPagedResponse);
        assertEquals(2, suggestResultPagedResponse.getValue().size());
        Hotel hotel = suggestResultPagedResponse.getValue().get(0).getDocument(Hotel.class);
        assertEquals("10", hotel.hotelId());
    }

    static void verifyCanSuggestStaticallyTypedDocuments(SuggestPagedResponse suggestResultPagedResponse,
        List<Map<String, Object>> expectedHotels) {
        //sanity
        assertNotNull(suggestResultPagedResponse);
        List<SearchDocument> docs = suggestResultPagedResponse.getValue()
            .stream()
            .map(suggestResult -> suggestResult.getDocument(SearchDocument.class))
            .collect(Collectors.toList());
        List<SuggestResult> hotelsList = suggestResultPagedResponse.getValue();

        List<SearchDocument> expectedHotelsList = expectedHotels.stream().map(SearchDocument::new)
            .filter(h -> h.get("HotelId").equals("10") || h.get("HotelId").equals("8"))
            .sorted(Comparator.comparing(h -> h.get("HotelId").toString())).collect(Collectors.toList());

        //assert
        //verify fields
        assertEquals(2, docs.size());
        assertEquals(hotelsList.stream().map(SuggestResult::getText).collect(Collectors.toList()),
            expectedHotelsList.stream().map(hotel -> hotel.get("Description")).collect(Collectors.toList()));
    }

    static void verifyMinimumCoverage(SuggestPagedResponse suggestResultPagedResponse) {
        assertNotNull(suggestResultPagedResponse);
        assertEquals(Double.valueOf(100.0), suggestResultPagedResponse.getCoverage());
    }

    static void verifyTopDocumentSuggest(SuggestPagedResponse suggestResultPagedResponse) {
        assertNotNull(suggestResultPagedResponse);
        assertEquals(3, suggestResultPagedResponse.getValue().size());
        List<String> resultIds = suggestResultPagedResponse
            .getValue()
            .stream()
            .map(hotel -> hotel.getDocument(Hotel.class).hotelId())
            .collect(Collectors.toList());

        assertEquals(Arrays.asList("1", "10", "2"), resultIds);
    }

    static void verifyCanSuggestWithDateTimeInStaticModel(SuggestPagedResponse suggestResultPagedResponse) {
        List<SuggestResult> books = suggestResultPagedResponse.getValue();

        assertEquals(1, books.size());
        assertEquals("War and Peace", books.get(0).getText());
    }

    @SuppressWarnings("unchecked")
    static void verifySuggestWithSelectedFields(PagedResponse<SuggestResult> suggestResultPagedResponse) {
        assertEquals(1, suggestResultPagedResponse.getValue().size());
        SearchDocument result = suggestResultPagedResponse.getValue().get(0).getDocument(SearchDocument.class);

        assertEquals("Secret Point Motel", result.get("HotelName"));
        assertEquals(4, ((Number) result.get("Rating")).intValue());
        assertEquals("New York", ((LinkedHashMap<?, ?>) result.get("Address")).get("City"));
        assertEquals(Arrays.asList("Budget Room", "Budget Room"),
            ((List<Map<String, String>>) result.get("Rooms"))
                .stream()
                .map(room -> room.get("Type"))
                .collect(Collectors.toList()));
    }
}
