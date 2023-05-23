// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents;

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
import com.azure.search.documents.util.SuggestPagedResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.HttpURLConnection;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.azure.search.documents.TestHelpers.assertHttpResponseException;
import static com.azure.search.documents.TestHelpers.readJsonFileToList;
import static com.azure.search.documents.TestHelpers.setupSharedIndex;
import static com.azure.search.documents.TestHelpers.uploadDocuments;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SuggestSyncTests extends SearchTestBase {
    private static final String BOOKS_INDEX_JSON = "BooksIndexData.json";
    private static final String INDEX_NAME = "azsearch-suggest-shared-instance";

    private final List<String> indexesToDelete = new ArrayList<>();

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
    public void canSuggestDynamicDocuments() {
        client = getSearchClientBuilder(INDEX_NAME).buildClient();

        SuggestOptions suggestOptions = new SuggestOptions()
            .setOrderBy("HotelId");

        Iterator<SuggestPagedResponse> suggestResultIterator = client.suggest("more",
            "sg", suggestOptions, Context.NONE)
            .iterableByPage()
            .iterator();

        verifyDynamicDocumentSuggest(suggestResultIterator.next());
        assertFalse(suggestResultIterator.hasNext());
    }

    @Test
    public void searchFieldsExcludesFieldsFromSuggest() {
        client = getSearchClientBuilder(INDEX_NAME).buildClient();

        SuggestOptions suggestOptions = new SuggestOptions()
            .setSearchFields("HotelName");

        Iterator<SuggestPagedResponse> suggestResultIterator = client.suggest("luxury",
            "sg", suggestOptions, Context.NONE)
            .iterableByPage()
            .iterator();

        verifyFieldsExcludesFieldsSuggest(suggestResultIterator.next());
        assertFalse(suggestResultIterator.hasNext());
    }

    @Test
    public void canUseSuggestHitHighlighting() {
        client = getSearchClientBuilder(INDEX_NAME).buildClient();

        SuggestOptions suggestOptions = new SuggestOptions()
            .setHighlightPreTag("<b>")
            .setHighlightPostTag("</b>")
            .setFilter("Category eq 'Luxury'")
            .setTop(1);

        Iterator<SuggestPagedResponse> suggestResultIterator = client.suggest("hotel",
            "sg", suggestOptions, Context.NONE)
            .iterableByPage()
            .iterator();

        verifyHitHighlightingSuggest(suggestResultIterator.next());
        assertFalse(suggestResultIterator.hasNext());
    }

    @Test
    public void canGetFuzzySuggestions() {
        client = getSearchClientBuilder(INDEX_NAME).buildClient();

        SuggestOptions suggestOptions = new SuggestOptions()
            .setUseFuzzyMatching(true);

        Iterator<SuggestPagedResponse> suggestResultIterator = client.suggest("hitel",
            "sg", suggestOptions, Context.NONE)
            .iterableByPage()
            .iterator();

        verifyFuzzySuggest(suggestResultIterator.next());
        assertFalse(suggestResultIterator.hasNext());
    }

    @Test
    public void canSuggestStaticallyTypedDocuments() {
        client = getSearchClientBuilder(INDEX_NAME).buildClient();

        List<Map<String, Object>> hotels = readJsonFileToList(HOTELS_DATA_JSON);
        //arrange
        SuggestOptions suggestOptions = new SuggestOptions()
            .setOrderBy("HotelId");

        //act
        Iterator<SuggestPagedResponse> suggestResultIterator = client.suggest("more",
            "sg", suggestOptions, Context.NONE)
            .iterableByPage()
            .iterator();
        //assert
        verifyCanSuggestStaticallyTypedDocuments(suggestResultIterator.next(), hotels);
    }

    @Test
    public void canSuggestWithDateTimeInStaticModel() {
        client = setupClient(() -> setupIndexFromJsonFile(BOOKS_INDEX_JSON));

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
        uploadDocuments(client, Arrays.asList(doc1, doc2));

        SuggestOptions suggestOptions = new SuggestOptions();
        suggestOptions.setSelect("ISBN", "Title", "PublishDate");
        Iterator<SuggestPagedResponse> suggestResultIterator = client.suggest("War",
            "sg", suggestOptions, Context.NONE)
            .iterableByPage()
            .iterator();

        //assert
        verifyCanSuggestWithDateTimeInStaticModel(suggestResultIterator.next());
    }

    @Test
    public void fuzzyIsOffByDefault() {
        client = getSearchClientBuilder(INDEX_NAME).buildClient();

        Iterator<SuggestPagedResponse> suggestResultIterator = client.suggest("hitel",
            "sg", null, Context.NONE)
            .iterableByPage()
            .iterator();

        verifyFuzzyIsOffByDefault(suggestResultIterator.next());

        Iterator<SuggestPagedResponse> suggestResultWithoutSuggestOptions = client.suggest("hitel", "sg")
            .iterableByPage()
            .iterator();

        verifyFuzzyIsOffByDefault(suggestResultWithoutSuggestOptions.next());
    }

    @Test
    public void suggestThrowsWhenGivenBadSuggesterName() {
        client = getSearchClientBuilder(INDEX_NAME).buildClient();

        PagedIterableBase<SuggestResult, SuggestPagedResponse> suggestResultIterator = client.suggest("Hotel",
            "Suggester does not exist", new SuggestOptions(), Context.NONE);

        assertHttpResponseException(
            () -> suggestResultIterator.iterableByPage().iterator().next(),
            HttpURLConnection.HTTP_BAD_REQUEST,
            "The specified suggester name 'Suggester does not exist' does not exist in this index definition.");
    }

    @Test
    public void suggestThrowsWhenRequestIsMalformed() {
        client = getSearchClientBuilder(INDEX_NAME).buildClient();

        SuggestOptions suggestOptions = new SuggestOptions().setOrderBy("This is not a valid orderby.");

        PagedIterableBase<SuggestResult, SuggestPagedResponse> suggestResultIterator = client.suggest("hotel",
            "sg", suggestOptions, Context.NONE);

        assertHttpResponseException(
            () -> suggestResultIterator.iterableByPage().iterator().next(),
            HttpURLConnection.HTTP_BAD_REQUEST,
            "Invalid expression: Syntax error at position 7 in 'This is not a valid orderby.'");
    }

    @Test
    public void testCanSuggestWithMinimumCoverage() {
        client = getSearchClientBuilder(INDEX_NAME).buildClient();

        //arrange
        SuggestOptions suggestOptions = new SuggestOptions()
            .setOrderBy("HotelId")
            .setMinimumCoverage(50.0);

        //act
        SuggestPagedResponse suggestPagedResponse = client.suggest("luxury",
            "sg", suggestOptions, Context.NONE)
            .iterableByPage()
            .iterator()
            .next();

        verifyMinimumCoverage(suggestPagedResponse);

    }

    @Test
    public void testTopTrimsResults() {
        client = getSearchClientBuilder(INDEX_NAME).buildClient();

        //arrange
        SuggestOptions suggestOptions = new SuggestOptions()
            .setOrderBy("HotelId")
            .setTop(3);

        //act
        Iterator<SuggestPagedResponse> suggestResultIterator = client.suggest("hotel",
            "sg", suggestOptions, Context.NONE)
            .iterableByPage()
            .iterator();

        //assert
        verifyTopDocumentSuggest(suggestResultIterator.next());
    }

    @Test
    public void testCanFilter() {
        client = getSearchClientBuilder(INDEX_NAME).buildClient();

        SuggestOptions suggestOptions = new SuggestOptions()
            .setFilter("Rating gt 3 and LastRenovationDate gt 2000-01-01T00:00:00Z")
            .setOrderBy("HotelId");

        SuggestPagedResponse suggestPagedResponse = client.suggest("hotel",
            "sg", suggestOptions, Context.NONE)
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
    public void testOrderByProgressivelyBreaksTies() {
        client = getSearchClientBuilder(INDEX_NAME).buildClient();

        SuggestOptions suggestOptions = new SuggestOptions()
            .setOrderBy(
                "Rating desc",
                "LastRenovationDate asc",
                "geo.distance(Location, geography'POINT(-122.0 49.0)')");

        SuggestPagedResponse suggestPagedResponse = client.suggest("hotel",
            "sg", suggestOptions, Context.NONE)
            .iterableByPage().iterator().next();

        assertNotNull(suggestPagedResponse);
        List<String> actualIds = suggestPagedResponse.getValue().stream()
            .map(s -> (String) s.getDocument(SearchDocument.class).get("HotelId")).collect(Collectors.toList());
        List<String> expectedIds = Arrays.asList("1", "9", "4", "3", "5");
        assertEquals(expectedIds, actualIds);
    }

    @Test
    public void testCanSuggestWithSelectedFields() {
        client = getSearchClientBuilder(INDEX_NAME).buildClient();

        SuggestOptions suggestOptions = new SuggestOptions()
            .setSelect("HotelName", "Rating", "Address/City", "Rooms/Type");
        PagedIterableBase<SuggestResult, SuggestPagedResponse> suggestResult = client.suggest("secret",
            "sg", suggestOptions, Context.NONE);

        PagedResponse<SuggestResult> result = suggestResult.iterableByPage().iterator().next();

        verifySuggestWithSelectedFields(result);

    }

    void verifyFuzzySuggest(SuggestPagedResponse suggestResultPagedResponse) {
        assertNotNull(suggestResultPagedResponse);
        assertEquals(5, suggestResultPagedResponse.getValue().size());
    }

    void verifyHitHighlightingSuggest(SuggestPagedResponse suggestResultPagedResponse) {
        assertNotNull(suggestResultPagedResponse);
        assertEquals(1, suggestResultPagedResponse.getValue().size());
        assertTrue(
            suggestResultPagedResponse.getValue()
                .get(0)
                .getText()
                .startsWith("Best <b>hotel</b> in town"));
    }

    void verifyFieldsExcludesFieldsSuggest(SuggestPagedResponse suggestResultPagedResponse) {
        assertNotNull(suggestResultPagedResponse);
        assertEquals(0, suggestResultPagedResponse.getValue().size());
    }

    void verifyDynamicDocumentSuggest(SuggestPagedResponse suggestResultPagedResponse) {
        assertNotNull(suggestResultPagedResponse);
        assertEquals(2, suggestResultPagedResponse.getValue().size());
        Hotel hotel = suggestResultPagedResponse.getValue().get(0).getDocument(Hotel.class);
        assertEquals("10", hotel.hotelId());
    }

    void verifyCanSuggestStaticallyTypedDocuments(SuggestPagedResponse suggestResultPagedResponse,
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

    void verifyFuzzyIsOffByDefault(SuggestPagedResponse suggestResultPagedResponse) {
        assertNotNull(suggestResultPagedResponse);
        assertEquals(0, suggestResultPagedResponse.getValue().size());
    }

    void verifyMinimumCoverage(SuggestPagedResponse suggestResultPagedResponse) {

        assertNotNull(suggestResultPagedResponse);
        assertEquals(Double.valueOf(100.0), suggestResultPagedResponse.getCoverage());
    }

    void verifyTopDocumentSuggest(SuggestPagedResponse suggestResultPagedResponse) {
        assertNotNull(suggestResultPagedResponse);
        assertEquals(3, suggestResultPagedResponse.getValue().size());
        List<String> resultIds = suggestResultPagedResponse
            .getValue()
            .stream()
            .map(hotel -> hotel.getDocument(Hotel.class).hotelId())
            .collect(Collectors.toList());

        assertEquals(Arrays.asList("1", "10", "2"), resultIds);
    }

    void verifyCanSuggestWithDateTimeInStaticModel(SuggestPagedResponse suggestResultPagedResponse) {
        List<SuggestResult> books = suggestResultPagedResponse.getValue();
        List<SearchDocument> docs = suggestResultPagedResponse.getValue()
            .stream()
            .map(suggestResult -> new SearchDocument(suggestResult.getDocument(SearchDocument.class)))
            .collect(Collectors.toList());

        assertEquals(1, docs.size());
        assertEquals("War and Peace", books.get(0).getText());
    }

    @SuppressWarnings("unchecked")
    void verifySuggestWithSelectedFields(PagedResponse<SuggestResult> suggestResultPagedResponse) {
        assertEquals(1, suggestResultPagedResponse.getValue().size());
        SearchDocument result = suggestResultPagedResponse.getValue().get(0).getDocument(SearchDocument.class);

        assertEquals("Secret Point Motel", result.get("HotelName"));
        assertEquals(4, result.get("Rating"));
        assertEquals("New York", ((LinkedHashMap) result.get("Address")).get("City"));
        assertEquals(Arrays.asList("Budget Room", "Budget Room"),
            ((ArrayList<LinkedHashMap<String, String>>) result.get("Rooms"))
                .parallelStream()
                .map(room -> room.get("Type"))
                .collect(Collectors.toList()));
    }
}
