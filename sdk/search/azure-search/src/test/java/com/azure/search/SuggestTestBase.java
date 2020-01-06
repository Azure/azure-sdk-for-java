// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import com.azure.core.http.rest.PagedResponse;
import com.azure.core.util.serializer.jsonwrapper.JsonWrapper;
import com.azure.core.util.serializer.jsonwrapper.api.JsonApi;
import com.azure.core.util.serializer.jsonwrapper.jacksonwrapper.JacksonDeserializer;
import com.azure.search.models.SuggestResult;
import com.azure.search.test.environment.models.Hotel;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class SuggestTestBase extends SearchIndexClientTestBase {
    private JsonApi jsonApi = JsonWrapper.newInstance(JacksonDeserializer.class);
    static final String BOOKS_INDEX_JSON = "BooksIndexData.json";
    static final String BOOKS_INDEX_NAME = "books";

    @Override
    protected void beforeTest() {
        super.beforeTest();
    }

    void verifyFuzzySuggest(SuggestPagedResponse suggestResultPagedResponse) {
        Assert.assertNotNull(suggestResultPagedResponse);
        Assert.assertEquals(5, suggestResultPagedResponse.getValue().size());
    }

    void verifyHitHighlightingSuggest(SuggestPagedResponse suggestResultPagedResponse) {
        Assert.assertNotNull(suggestResultPagedResponse);
        Assert.assertEquals(1, suggestResultPagedResponse.getValue().size());
        Assert.assertTrue(
            suggestResultPagedResponse.getValue()
                .get(0)
                .getText()
                .startsWith("Best <b>hotel</b> in town"));
    }

    void verifyFieldsExcludesFieldsSuggest(SuggestPagedResponse suggestResultPagedResponse) {
        Assert.assertNotNull(suggestResultPagedResponse);
        Assert.assertEquals(0, suggestResultPagedResponse.getValue().size());
    }

    void verifyDynamicDocumentSuggest(SuggestPagedResponse suggestResultPagedResponse) {
        Assert.assertNotNull(suggestResultPagedResponse);
        Assert.assertEquals(2, suggestResultPagedResponse.getValue().size());
        Hotel hotel = convertToType(suggestResultPagedResponse.getValue().get(0).getDocument(), Hotel.class);
        Assert.assertEquals("10", hotel.hotelId());
    }

    void verifyCanSuggestStaticallyTypedDocuments(SuggestPagedResponse suggestResultPagedResponse, List<Map<String, Object>> expectedHotels) {
        //sanity
        Assert.assertNotNull(suggestResultPagedResponse);
        List<Document> docs = suggestResultPagedResponse.getValue()
            .stream()
            .map(SuggestResult::getDocument)
            .collect(Collectors.toList());
        List<SuggestResult> hotelsList = suggestResultPagedResponse.getValue();
        List<Hotel> expectedHotelsList = expectedHotels.stream().map(hotel ->
            jsonApi.convertObjectToType(hotel, Hotel.class))
            .filter(h -> h.hotelId().equals("10") || h.hotelId().equals("8"))
            .sorted(Comparator.comparing(Hotel::hotelId)).collect(Collectors.toList());

        //assert
        //verify fields
        Assert.assertEquals(2, docs.size());
        Assert.assertEquals(hotelsList.stream().map(SuggestResult::getText).collect(Collectors.toList()),
            expectedHotelsList.stream().map(Hotel::description).collect(Collectors.toList()));
    }

    void verifyFuzzyIsOffByDefault(SuggestPagedResponse suggestResultPagedResponse) {
        Assert.assertNotNull(suggestResultPagedResponse);
        Assert.assertEquals(0, suggestResultPagedResponse.getValue().size());
    }

    void verifyMinimumCoverage(SuggestPagedResponse suggestResultPagedResponse) {

        Assert.assertNotNull(suggestResultPagedResponse);
        Assert.assertEquals(Double.valueOf(100.0), suggestResultPagedResponse.getCoverage());
    }

    void verifyTopDocumentSuggest(SuggestPagedResponse suggestResultPagedResponse) {
        Assert.assertNotNull(suggestResultPagedResponse);
        Assert.assertEquals(3, suggestResultPagedResponse.getValue().size());
        List<String> resultIds = suggestResultPagedResponse
            .getValue()
            .stream()
            .map(hotel -> convertToType(hotel.getDocument(), Hotel.class).hotelId())
            .collect(Collectors.toList());

        Assert.assertEquals(Arrays.asList("1", "10", "2"), resultIds);
    }

    void verifyCanSuggestWithDateTimeInStaticModel(SuggestPagedResponse suggestResultPagedResponse) {
        List<SuggestResult> books = suggestResultPagedResponse.getValue();
        List<Document> docs = suggestResultPagedResponse.getValue()
            .stream()
            .map(SuggestResult::getDocument)
            .collect(Collectors.toList());

        Assert.assertEquals(1, docs.size());
        Assert.assertEquals("War and Peace", books.get(0).getText());
    }

    @SuppressWarnings("unchecked")
    void verifySuggestWithSelectedFields(PagedResponse<SuggestResult> suggestResultPagedResponse) {
        Assert.assertEquals(1, suggestResultPagedResponse.getValue().size());
        Document result = suggestResultPagedResponse.getValue().get(0).getDocument();

        Assert.assertEquals("Secret Point Motel", result.get("HotelName"));
        Assert.assertEquals(4, result.get("Rating"));
        Assert.assertEquals("New York", ((LinkedHashMap) result.get("Address")).get("City"));
        Assert.assertEquals(Arrays.asList("Budget Room", "Budget Room"),
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
