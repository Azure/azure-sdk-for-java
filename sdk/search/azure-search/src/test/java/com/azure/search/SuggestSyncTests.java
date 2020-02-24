// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import com.azure.core.http.rest.PagedIterableBase;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.util.Context;
import com.azure.search.models.SuggestOptions;
import com.azure.search.models.SuggestResult;
import com.azure.search.test.environment.models.Author;
import com.azure.search.test.environment.models.Book;
import com.azure.search.test.environment.models.Hotel;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;

import static com.azure.search.SearchTestBase.HOTELS_DATA_JSON;
import static com.azure.search.SearchTestBase.HOTELS_INDEX_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SuggestSyncTests extends SearchIndexClientTestBase {
    private static final String BOOKS_INDEX_JSON = "BooksIndexData.json";
    private static final String BOOKS_INDEX_NAME = "books";

    private SearchIndexClient client;

    @Test
    public void canSuggestDynamicDocuments() {
        createHotelIndex();
        client = getSearchIndexClientBuilder(HOTELS_INDEX_NAME).buildClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);
        SuggestOptions suggestOptions = new SuggestOptions()
            .setOrderBy("HotelId");

        Iterator<SuggestPagedResponse> suggestResultIterator = client.suggest("more",
            "sg", suggestOptions, generateRequestOptions(), Context.NONE)
            .iterableByPage()
            .iterator();

        verifyDynamicDocumentSuggest(suggestResultIterator.next());
        assertFalse(suggestResultIterator.hasNext());
    }

    @Test
    public void searchFieldsExcludesFieldsFromSuggest() {
        createHotelIndex();
        client = getSearchIndexClientBuilder(HOTELS_INDEX_NAME).buildClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);
        SuggestOptions suggestOptions = new SuggestOptions()
            .setSearchFields("HotelName");

        Iterator<SuggestPagedResponse> suggestResultIterator = client.suggest("luxury",
            "sg", suggestOptions, generateRequestOptions(), Context.NONE)
            .iterableByPage()
            .iterator();

        verifyFieldsExcludesFieldsSuggest(suggestResultIterator.next());
        assertFalse(suggestResultIterator.hasNext());
    }

    @Test
    public void canUseSuggestHitHighlighting() {
        createHotelIndex();
        client = getSearchIndexClientBuilder(HOTELS_INDEX_NAME).buildClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);
        SuggestOptions suggestOptions = new SuggestOptions()
            .setHighlightPreTag("<b>")
            .setHighlightPostTag("</b>")
            .setFilter("Category eq 'Luxury'")
            .setTop(1);

        Iterator<SuggestPagedResponse> suggestResultIterator = client.suggest("hotel",
            "sg", suggestOptions, generateRequestOptions(), Context.NONE)
            .iterableByPage()
            .iterator();

        verifyHitHighlightingSuggest(suggestResultIterator.next());
        assertFalse(suggestResultIterator.hasNext());
    }

    @Test
    public void canGetFuzzySuggestions() {
        createHotelIndex();
        client = getSearchIndexClientBuilder(HOTELS_INDEX_NAME).buildClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);
        SuggestOptions suggestOptions = new SuggestOptions()
            .setUseFuzzyMatching(true);

        Iterator<SuggestPagedResponse> suggestResultIterator = client.suggest("hitel",
            "sg", suggestOptions, generateRequestOptions(), Context.NONE)
            .iterableByPage()
            .iterator();

        verifyFuzzySuggest(suggestResultIterator.next());
        assertFalse(suggestResultIterator.hasNext());
    }

    @Test
    public void canSuggestStaticallyTypedDocuments() {
        createHotelIndex();
        client = getSearchIndexClientBuilder(HOTELS_INDEX_NAME).buildClient();

        List<Map<String, Object>> hotels = uploadDocumentsJson(client, HOTELS_DATA_JSON);
        //arrange
        SuggestOptions suggestOptions = new SuggestOptions()
            .setOrderBy("HotelId");

        //act
        Iterator<SuggestPagedResponse> suggestResultIterator = client.suggest("more",
            "sg", suggestOptions, generateRequestOptions(), Context.NONE)
            .iterableByPage()
            .iterator();
        //assert
        verifyCanSuggestStaticallyTypedDocuments(suggestResultIterator.next(), hotels);
    }

    @Test
    public void canSuggestWithDateTimeInStaticModel() {
        setupIndexFromJsonFile(BOOKS_INDEX_JSON);
        client = getSearchIndexClientBuilder(BOOKS_INDEX_NAME).buildClient();

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
            "sg", suggestOptions, generateRequestOptions(), Context.NONE)
            .iterableByPage()
            .iterator();

        //assert
        verifyCanSuggestWithDateTimeInStaticModel(suggestResultIterator.next());
    }

    @Test
    public void fuzzyIsOffByDefault() {
        createHotelIndex();
        client = getSearchIndexClientBuilder(HOTELS_INDEX_NAME).buildClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);

        Iterator<SuggestPagedResponse> suggestResultIterator = client.suggest("hitel",
            "sg", null, generateRequestOptions(), Context.NONE)
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
        createHotelIndex();
        client = getSearchIndexClientBuilder(HOTELS_INDEX_NAME).buildClient();

        PagedIterableBase<SuggestResult, SuggestPagedResponse> suggestResultIterator = client.suggest("Hotel",
            "Suggester does not exist", new SuggestOptions(), generateRequestOptions(), Context.NONE);

        assertHttpResponseException(
            () -> suggestResultIterator.iterableByPage().iterator().next(),
            HttpResponseStatus.BAD_REQUEST,
            "The specified suggester name 'Suggester does not exist' does not exist in this index definition.");
    }

    @Test
    public void suggestThrowsWhenRequestIsMalformed() {
        createHotelIndex();
        client = getSearchIndexClientBuilder(HOTELS_INDEX_NAME).buildClient();

        SuggestOptions suggestOptions = new SuggestOptions().setOrderBy("This is not a valid orderby.");

        PagedIterableBase<SuggestResult, SuggestPagedResponse> suggestResultIterator = client.suggest("hotel",
            "sg", suggestOptions, generateRequestOptions(), Context.NONE);

        assertHttpResponseException(
            () -> suggestResultIterator.iterableByPage().iterator().next(),
            HttpResponseStatus.BAD_REQUEST,
            "Invalid expression: Syntax error at position 7 in 'This is not a valid orderby.'");
    }

    @Test
    public void testCanSuggestWithMinimumCoverage() {
        createHotelIndex();
        client = getSearchIndexClientBuilder(HOTELS_INDEX_NAME).buildClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);

        //arrange
        SuggestOptions suggestOptions = new SuggestOptions()
            .setOrderBy("HotelId")
            .setMinimumCoverage(50.0);

        //act
        SuggestPagedResponse suggestPagedResponse = client.suggest("luxury",
            "sg", suggestOptions, generateRequestOptions(), Context.NONE)
            .iterableByPage()
            .iterator()
            .next();

        verifyMinimumCoverage(suggestPagedResponse);

    }

    @Test
    public void testTopTrimsResults() {
        createHotelIndex();
        client = getSearchIndexClientBuilder(HOTELS_INDEX_NAME).buildClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);
        //arrange
        SuggestOptions suggestOptions = new SuggestOptions()
            .setOrderBy("HotelId")
            .setTop(3);

        //act
        Iterator<SuggestPagedResponse> suggestResultIterator = client.suggest("hotel",
            "sg", suggestOptions, generateRequestOptions(), Context.NONE)
            .iterableByPage()
            .iterator();

        //assert
        verifyTopDocumentSuggest(suggestResultIterator.next());
    }

    @Test
    public void testCanFilter() {
        createHotelIndex();
        client = getSearchIndexClientBuilder(HOTELS_INDEX_NAME).buildClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);

        SuggestOptions suggestOptions = new SuggestOptions()
            .setFilter("Rating gt 3 and LastRenovationDate gt 2000-01-01T00:00:00Z")
            .setOrderBy("HotelId");

        SuggestPagedResponse suggestPagedResponse = client.suggest("hotel",
            "sg", suggestOptions, generateRequestOptions(), Context.NONE)
            .iterableByPage()
            .iterator()
            .next();

        assertNotNull(suggestPagedResponse);
        List<String> actualIds = suggestPagedResponse.getValue().stream()
            .map(s -> (String) s.getDocument().get("HotelId")).collect(Collectors.toList());
        List<String> expectedIds = Arrays.asList("1", "5");
        assertEquals(expectedIds, actualIds);
    }

    @Test
    public void testOrderByProgressivelyBreaksTies() {
        createHotelIndex();
        client = getSearchIndexClientBuilder(HOTELS_INDEX_NAME).buildClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);

        SuggestOptions suggestOptions = new SuggestOptions()
            .setOrderBy(
                "Rating desc",
                "LastRenovationDate asc",
                "geo.distance(Location, geography'POINT(-122.0 49.0)')");

        SuggestPagedResponse suggestPagedResponse = client.suggest("hotel",
            "sg", suggestOptions, generateRequestOptions(), Context.NONE)
            .iterableByPage().iterator().next();

        assertNotNull(suggestPagedResponse);
        List<String> actualIds = suggestPagedResponse.getValue().stream()
            .map(s -> (String) s.getDocument().get("HotelId")).collect(Collectors.toList());
        List<String> expectedIds = Arrays.asList("1", "9", "4", "3", "5");
        assertEquals(expectedIds, actualIds);
    }

    @Test
    public void testCanSuggestWithSelectedFields() {
        createHotelIndex();
        client = getClientBuilder(HOTELS_INDEX_NAME).buildClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);

        SuggestOptions suggestOptions = new SuggestOptions()
            .setSelect("HotelName", "Rating", "Address/City", "Rooms/Type");
        PagedIterableBase<SuggestResult, SuggestPagedResponse> suggestResult = client.suggest("secret",
            "sg", suggestOptions, generateRequestOptions(), Context.NONE);

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
        Hotel hotel = convertToType(suggestResultPagedResponse.getValue().get(0).getDocument(), Hotel.class);
        assertEquals("10", hotel.hotelId());
    }

    void verifyCanSuggestStaticallyTypedDocuments(SuggestPagedResponse suggestResultPagedResponse, List<Map<String, Object>> expectedHotels) {
        //sanity
        assertNotNull(suggestResultPagedResponse);
        List<Document> docs = suggestResultPagedResponse.getValue()
            .stream()
            .map(SuggestResult::getDocument)
            .collect(Collectors.toList());
        List<SuggestResult> hotelsList = suggestResultPagedResponse.getValue();

        ObjectMapper objectMapper = new ObjectMapper();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(TimeZone.getDefault());
        objectMapper.setDateFormat(df);
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        List<Hotel> expectedHotelsList = expectedHotels.stream().map(hotel ->
            objectMapper.convertValue(hotel, Hotel.class))
            .filter(h -> h.hotelId().equals("10") || h.hotelId().equals("8"))
            .sorted(Comparator.comparing(Hotel::hotelId)).collect(Collectors.toList());

        //assert
        //verify fields
        assertEquals(2, docs.size());
        assertEquals(hotelsList.stream().map(SuggestResult::getText).collect(Collectors.toList()),
            expectedHotelsList.stream().map(Hotel::description).collect(Collectors.toList()));
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
            .map(hotel -> convertToType(hotel.getDocument(), Hotel.class).hotelId())
            .collect(Collectors.toList());

        assertEquals(Arrays.asList("1", "10", "2"), resultIds);
    }

    void verifyCanSuggestWithDateTimeInStaticModel(SuggestPagedResponse suggestResultPagedResponse) {
        List<SuggestResult> books = suggestResultPagedResponse.getValue();
        List<Document> docs = suggestResultPagedResponse.getValue()
            .stream()
            .map(SuggestResult::getDocument)
            .collect(Collectors.toList());

        assertEquals(1, docs.size());
        assertEquals("War and Peace", books.get(0).getText());
    }

    @SuppressWarnings("unchecked")
    void verifySuggestWithSelectedFields(PagedResponse<SuggestResult> suggestResultPagedResponse) {
        assertEquals(1, suggestResultPagedResponse.getValue().size());
        Document result = suggestResultPagedResponse.getValue().get(0).getDocument();

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
