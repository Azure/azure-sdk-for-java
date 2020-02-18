// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import com.azure.core.http.rest.PagedResponse;
import com.azure.search.models.SuggestResult;
import com.azure.search.test.environment.models.Hotel;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class SuggestTestBase extends SearchIndexClientTestBase {
    static final String BOOKS_INDEX_JSON = "BooksIndexData.json";
    static final String BOOKS_INDEX_NAME = "books";

    @Override
    protected void beforeTest() {
        super.beforeTest();
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

    @Test
    public abstract void canSuggestDynamicDocuments() throws IOException;

    @Test
    public abstract void searchFieldsExcludesFieldsFromSuggest() throws IOException;

    @Test
    public abstract void canUseSuggestHitHighlighting() throws IOException;

    @Test
    public abstract void canGetFuzzySuggestions() throws IOException;

    @Test
    public abstract void canSuggestStaticallyTypedDocuments() throws IOException;

    @Test
    public abstract void canSuggestWithDateTimeInStaticModel();

    @Test
    public abstract void fuzzyIsOffByDefault() throws IOException;

    @Test
    public abstract void suggestThrowsWhenGivenBadSuggesterName();

    @Test
    public abstract void suggestThrowsWhenRequestIsMalformed() throws IOException;

    @Test
    public abstract void testCanSuggestWithMinimumCoverage() throws IOException;

    @Test
    public abstract void testTopTrimsResults() throws IOException;

    @Test
    public abstract void testCanFilter() throws IOException;

    @Test
    public abstract void testOrderByProgressivelyBreaksTies() throws IOException;

    @Test
    public abstract void testCanSuggestWithSelectedFields() throws IOException;
}
