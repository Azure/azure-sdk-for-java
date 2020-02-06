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
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.azure.search.SearchTestBase.HOTELS_DATA_JSON;
import static com.azure.search.SearchTestBase.HOTELS_INDEX_NAME;

public class SuggestSyncTests extends SuggestTestBase {

    private SearchIndexClient client;

    @Test
    public void canSuggestDynamicDocuments() throws IOException {
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
        Assert.assertFalse(suggestResultIterator.hasNext());
    }

    @Test
    public void searchFieldsExcludesFieldsFromSuggest() throws IOException {
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
        Assert.assertFalse(suggestResultIterator.hasNext());
    }

    @Test
    public void canUseSuggestHitHighlighting() throws IOException {
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
        Assert.assertFalse(suggestResultIterator.hasNext());
    }

    @Test
    public void canGetFuzzySuggestions() throws IOException {
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
        Assert.assertFalse(suggestResultIterator.hasNext());
    }

    @Test
    public void canSuggestStaticallyTypedDocuments() throws IOException {
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
    public void fuzzyIsOffByDefault() throws IOException {
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
    public void testCanSuggestWithMinimumCoverage() throws IOException {
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
    public void testTopTrimsResults() throws IOException {
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
    public void testCanFilter() throws IOException {
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

        Assert.assertNotNull(suggestPagedResponse);
        List<String> actualIds = suggestPagedResponse.getValue().stream()
            .map(s -> (String) s.getDocument().get("HotelId")).collect(Collectors.toList());
        List<String> expectedIds = Arrays.asList("1", "5");
        Assert.assertEquals(expectedIds, actualIds);
    }

    @Test
    public void testOrderByProgressivelyBreaksTies() throws IOException {
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

        Assert.assertNotNull(suggestPagedResponse);
        List<String> actualIds = suggestPagedResponse.getValue().stream()
            .map(s -> (String) s.getDocument().get("HotelId")).collect(Collectors.toList());
        List<String> expectedIds = Arrays.asList("1", "9", "4", "3", "5");
        Assert.assertEquals(expectedIds, actualIds);
    }

    @Test
    public void testCanSuggestWithSelectedFields() throws IOException {
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
}
