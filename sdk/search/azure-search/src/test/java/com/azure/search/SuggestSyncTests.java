// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.PagedIterableBase;
import com.azure.search.common.SuggestPagedResponse;
import com.azure.search.models.SuggestOptions;
import com.azure.search.models.SuggestResult;
import com.azure.search.test.environment.models.Author;
import com.azure.search.test.environment.models.Book;
import org.junit.Assert;
import org.junit.Test;

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
    public void canSuggestDynamicDocuments() {
        createHotelIndex();
        client = getClientBuilder(HOTELS_INDEX_NAME).buildClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);
        SuggestOptions suggestOptions = new SuggestOptions()
            .setOrderBy("HotelId");

        Iterator<SuggestPagedResponse> suggestResultIterator = client.suggest("more", "sg", suggestOptions, null)
            .iterableByPage()
            .iterator();

        verifyDynamicDocumentSuggest(suggestResultIterator.next());
        Assert.assertFalse(suggestResultIterator.hasNext());
    }

    @Test
    public void searchFieldsExcludesFieldsFromSuggest() {
        createHotelIndex();
        client = getClientBuilder(HOTELS_INDEX_NAME).buildClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);
        SuggestOptions suggestOptions = new SuggestOptions()
            .setSearchFields("HotelName");

        Iterator<SuggestPagedResponse> suggestResultIterator = client.suggest("luxury", "sg", suggestOptions, null)
            .iterableByPage()
            .iterator();

        verifyFieldsExcludesFieldsSuggest(suggestResultIterator.next());
        Assert.assertFalse(suggestResultIterator.hasNext());
    }

    @Test
    public void canUseSuggestHitHighlighting() {
        createHotelIndex();
        client = getClientBuilder(HOTELS_INDEX_NAME).buildClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);
        SuggestOptions suggestOptions = new SuggestOptions()
            .setHighlightPreTag("<b>")
            .setHighlightPostTag("</b>")
            .setFilter("Category eq 'Luxury'")
            .setTop(1);

        Iterator<SuggestPagedResponse> suggestResultIterator = client.suggest("hotel", "sg", suggestOptions, null)
            .iterableByPage()
            .iterator();

        verifyHitHighlightingSuggest(suggestResultIterator.next());
        Assert.assertFalse(suggestResultIterator.hasNext());
    }

    @Test
    public void canGetFuzzySuggestions() {
        createHotelIndex();
        client = getClientBuilder(HOTELS_INDEX_NAME).buildClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);
        SuggestOptions suggestOptions = new SuggestOptions()
            .setUseFuzzyMatching(true);

        Iterator<SuggestPagedResponse> suggestResultIterator = client.suggest("hitel", "sg", suggestOptions, null)
            .iterableByPage()
            .iterator();

        verifyFuzzySuggest(suggestResultIterator.next());
        Assert.assertFalse(suggestResultIterator.hasNext());
    }

    @Override
    public void canSuggestStaticallyTypedDocuments() {
        createHotelIndex();
        client = getClientBuilder(HOTELS_INDEX_NAME).buildClient();

        List<Map<String, Object>> hotels = uploadDocumentsJson(client, HOTELS_DATA_JSON);
        //arrange
        SuggestOptions suggestOptions = new SuggestOptions()
            .setOrderBy("HotelId");

        //act
        Iterator<SuggestPagedResponse> suggestResultIterator = client.suggest("more", "sg", suggestOptions, null)
            .iterableByPage()
            .iterator();
        //assert
        verifyCanSuggestStaticallyTypedDocuments(suggestResultIterator.next(), hotels);
    }

    @Override
    public void canSuggestWithDateTimeInStaticModel() {
        setupIndexFromJsonFile(BOOKS_INDEX_JSON);
        client = getClientBuilder(BOOKS_INDEX_NAME).buildClient();

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
        Iterator<SuggestPagedResponse> suggestResultIterator = client.suggest("War", "sg", suggestOptions, null)
            .iterableByPage()
            .iterator();

        //assert
        verifyCanSuggestWithDateTimeInStaticModel(suggestResultIterator.next());
    }

    @Override
    public void fuzzyIsOffByDefault() {
        createHotelIndex();
        client = getClientBuilder(HOTELS_INDEX_NAME).buildClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);

        Iterator<SuggestPagedResponse> suggestResultIterator = client.suggest("hitel", "sg", null, null)
            .iterableByPage()
            .iterator();

        verifyFuzzyIsOffByDefault(suggestResultIterator.next());
    }

    @Override
    public void suggestThrowsWhenGivenBadSuggesterName() {
        createHotelIndex();
        client = getClientBuilder(HOTELS_INDEX_NAME).buildClient();

        PagedIterableBase<SuggestResult, SuggestPagedResponse> suggestResultIterator =
            client.suggest("Hotel", "Suggester does not exist", null, null);

        assertException(
            () -> suggestResultIterator.iterableByPage().iterator().next(),
            HttpResponseException.class,
            "The specified suggester name 'Suggester does not exist' does not exist in this index definition.");
    }

    @Override
    public void suggestThrowsWhenRequestIsMalformed() {
        createHotelIndex();
        client = getClientBuilder(HOTELS_INDEX_NAME).buildClient();

        SuggestOptions suggestOptions = new SuggestOptions().setOrderBy("This is not a valid orderby.");

        PagedIterableBase<SuggestResult, SuggestPagedResponse> suggestResultIterator =
            client.suggest("hotel", "sg", suggestOptions, null);

        assertException(
            () -> suggestResultIterator.iterableByPage().iterator().next(),
            HttpResponseException.class,
            "Invalid expression: Syntax error at position 7 in 'This is not a valid orderby.'");
    }

    @Override
    public void testCanSuggestWithMinimumCoverage() {
        createHotelIndex();
        client = getClientBuilder(HOTELS_INDEX_NAME).buildClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);

        //arrange
        SuggestOptions suggestOptions = new SuggestOptions()
            .setOrderBy("HotelId")
            .setMinimumCoverage(50.0);

        //act
        SuggestPagedResponse suggestPagedResponse = client
            .suggest("luxury", "sg", suggestOptions, null)
            .iterableByPage()
            .iterator()
            .next();

        verifyMinimumCoverage(suggestPagedResponse);

    }

    @Override
    public void testTopTrimsResults() {
        createHotelIndex();
        client = getClientBuilder(HOTELS_INDEX_NAME).buildClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);
        //arrange
        SuggestOptions suggestOptions = new SuggestOptions()
            .setOrderBy("HotelId")
            .setTop(3);

        //act
        Iterator<SuggestPagedResponse> suggestResultIterator = client.suggest(
            "hotel",
            "sg",
            suggestOptions,
            null)
            .iterableByPage()
            .iterator();

        //assert
        verifyTopDocumentSuggest(suggestResultIterator.next());
    }

    @Override
    public void testCanFilter() {
        createHotelIndex();
        client = getClientBuilder(HOTELS_INDEX_NAME).buildClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);

        SuggestOptions suggestOptions = new SuggestOptions()
            .setFilter("Rating gt 3 and LastRenovationDate gt 2000-01-01T00:00:00Z")
            .setOrderBy("HotelId");

        SuggestPagedResponse suggestPagedResponse = client.suggest("hotel", "sg", suggestOptions, null)
            .iterableByPage()
            .iterator()
            .next();

        Assert.assertNotNull(suggestPagedResponse);
        List<String> actualIds = suggestPagedResponse.getValue().stream()
            .map(s -> (String) s.getDocument().get("HotelId")).collect(Collectors.toList());
        List<String> expectedIds = Arrays.asList("1", "5");
        Assert.assertEquals(expectedIds, actualIds);
    }

    @Override
    public void testOrderByProgressivelyBreaksTies() {
        createHotelIndex();
        client = getClientBuilder(HOTELS_INDEX_NAME).buildClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);

        SuggestOptions suggestOptions = new SuggestOptions()
            .setOrderBy(
                "Rating desc",
                "LastRenovationDate asc",
                "geo.distance(Location, geography'POINT(-122.0 49.0)')");

        SuggestPagedResponse suggestPagedResponse = client.suggest("hotel", "sg", suggestOptions, null)
            .iterableByPage().iterator().next();

        Assert.assertNotNull(suggestPagedResponse);
        List<String> actualIds = suggestPagedResponse.getValue().stream()
            .map(s -> (String) s.getDocument().get("HotelId")).collect(Collectors.toList());
        List<String> expectedIds = Arrays.asList("1", "9", "4", "3", "5");
        Assert.assertEquals(expectedIds, actualIds);
    }
}
