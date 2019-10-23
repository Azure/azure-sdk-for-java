// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.search.common.SuggestPagedResponse;
import com.azure.search.models.SuggestOptions;
import com.azure.search.models.SuggestResult;
import com.azure.search.test.environment.models.Author;
import com.azure.search.test.environment.models.Book;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

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

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void canSuggestDynamicDocuments() {
        createHotelIndex();
        client = getClientBuilder(HOTELS_INDEX_NAME).buildClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);
        SuggestOptions suggestOptions = new SuggestOptions()
            .setOrderBy("HotelId");

        PagedIterable<SuggestResult> suggestResult = client.suggest("more", "sg", suggestOptions, null);

        Iterable<SuggestPagedResponse> pagesIterable = suggestResult.iterableByPage();
        Iterator<SuggestPagedResponse> iterator = pagesIterable.iterator();

        verifyDynamicDocumentSuggest(iterator.next());
        Assert.assertFalse(iterator.hasNext());
    }

    @Test
    public void searchFieldsExcludesFieldsFromSuggest() {
        createHotelIndex();
        client = getClientBuilder(HOTELS_INDEX_NAME).buildClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);
        SuggestOptions suggestOptions = new SuggestOptions()
            .setSearchFields("HotelName");

        PagedIterable<SuggestResult> suggestResult = client.suggest("luxury", "sg", suggestOptions, null);

        Iterable<SuggestPagedResponse> pagesIterable = suggestResult.iterableByPage();
        Iterator<SuggestPagedResponse> iterator = pagesIterable.iterator();


        verifyFieldsExcludesFieldsSuggest(iterator.next());
        Assert.assertFalse(iterator.hasNext());
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

        PagedIterable<SuggestResult> suggestResult = client.suggest("hotel", "sg", suggestOptions, null);

        Iterable<SuggestPagedResponse> pagesIterable = suggestResult.iterableByPage();
        Iterator<SuggestPagedResponse> iterator = pagesIterable.iterator();


        verifyHitHighlightingSuggest(iterator.next());
        Assert.assertFalse(iterator.hasNext());
    }

    @Test
    public void canGetFuzzySuggestions() {
        createHotelIndex();
        client = getClientBuilder(HOTELS_INDEX_NAME).buildClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);
        SuggestOptions suggestOptions = new SuggestOptions()
            .setUseFuzzyMatching(true);

        PagedIterable<SuggestResult> suggestResult = client.suggest("hitel", "sg", suggestOptions, null);

        Iterable<SuggestPagedResponse> pagesIterable = suggestResult.iterableByPage();
        Iterator<SuggestPagedResponse> iterator = pagesIterable.iterator();

        verifyFuzzySuggest(iterator.next());
        Assert.assertFalse(iterator.hasNext());
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
        PagedIterable<SuggestResult> suggestResult = client.suggest("more", "sg", suggestOptions, null);

        Iterable<SuggestPagedResponse> pagesIterable = suggestResult.iterableByPage();
        Iterator<SuggestPagedResponse> iterator = pagesIterable.iterator();

        //assert
        verifyCanSuggestStaticallyTypedDocuments(iterator.next(), hotels);
    }

    @Override
    public void canSuggestWithDateTimeInStaticModel() throws Exception {
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
        PagedIterable<SuggestResult> suggestResult = client.suggest("War", "sg", suggestOptions, null);

        Iterable<SuggestPagedResponse> pagesIterable = suggestResult.iterableByPage();
        Iterator<SuggestPagedResponse> iterator = pagesIterable.iterator();

        //assert
        verifyCanSuggestWithDateTimeInStaticModel(iterator.next());
    }

    @Override
    public void fuzzyIsOffByDefault() {
        createHotelIndex();
        client = getClientBuilder(HOTELS_INDEX_NAME).buildClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);

        PagedIterable<SuggestResult> suggestResult = client.suggest("hitel", "sg", null, null);

        Iterable<SuggestPagedResponse> pagesIterable = suggestResult.iterableByPage();
        Iterator<SuggestPagedResponse> iterator = pagesIterable.iterator();

        verifyFuzzyIsOffByDefault(iterator.next());
    }

    @Override
    public void suggestThrowsWhenGivenBadSuggesterName() {
        createHotelIndex();
        client = getClientBuilder(HOTELS_INDEX_NAME).buildClient();

        thrown.expect(HttpResponseException.class);
        thrown.expectMessage("The specified suggester name 'Suggester does not exist' "
            + "does not exist in this index definition.");

        PagedIterable<SuggestResult> suggestResult = client.suggest("Hotel", "Suggester does not exist", null, null);
        suggestResult.iterableByPage().iterator().next();
    }

    @Override
    public void suggestThrowsWhenRequestIsMalformed() {
        createHotelIndex();
        client = getClientBuilder(HOTELS_INDEX_NAME).buildClient();

        thrown.expect(HttpResponseException.class);
        thrown.expectMessage("Invalid expression: Syntax error at position 7 in 'This is not a valid orderby.'");

        uploadDocumentsJson(client, HOTELS_DATA_JSON);
        SuggestOptions suggestOptions = new SuggestOptions()
            .setOrderBy("This is not a valid orderby.");

        PagedIterable<SuggestResult> suggestResult = client.suggest("hotel", "sg", suggestOptions, null);
        suggestResult.iterableByPage().iterator().next();
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
        PagedIterable<SuggestResult> suggestResult = client
            .suggest("luxury", "sg", suggestOptions, null);

        Iterable<SuggestPagedResponse> pagesIterable = suggestResult.iterableByPage();
        SuggestPagedResponse suggestPagedResponse = pagesIterable.iterator().next();

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
        PagedIterable<SuggestResult> suggestResult = client.suggest("hotel",
            "sg",
            suggestOptions,
            null);

        Iterable<SuggestPagedResponse> pagesIterable = suggestResult.iterableByPage();
        Iterator<SuggestPagedResponse> iterator = pagesIterable.iterator();

        //assert
        verifyTopDocumentSuggest(iterator.next());
    }

    @Override
    public void testCanFilter() {
        createHotelIndex();
        client = getClientBuilder(HOTELS_INDEX_NAME).buildClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);

        SuggestOptions suggestOptions = new SuggestOptions()
            .setFilter("Rating gt 3 and LastRenovationDate gt 2000-01-01T00:00:00Z")
            .setOrderBy("HotelId");

        PagedIterable<SuggestResult> suggestResult = client.suggest("hotel", "sg", suggestOptions, null);
        PagedResponse<SuggestResult> result = suggestResult.iterableByPage().iterator().next();

        Assert.assertNotNull(result);
        List<String> actualIds = result.getValue().stream().map(s -> (String) s.getDocument().get("HotelId")).collect(Collectors.toList());
        List<String> expectedIds = Arrays.asList("1", "5");
        Assert.assertEquals(expectedIds, actualIds);
    }

    @Override
    public void testOrderByProgressivelyBreaksTies() {
        createHotelIndex();
        client = getClientBuilder(HOTELS_INDEX_NAME).buildClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);

        SuggestOptions suggestOptions = new SuggestOptions()
            .setOrderBy("Rating desc",
                "LastRenovationDate asc",
                "geo.distance(Location, geography'POINT(-122.0 49.0)')");

        PagedIterable<SuggestResult> suggestResult = client.suggest("hotel", "sg", suggestOptions, null);
        PagedResponse<SuggestResult> result = suggestResult.iterableByPage().iterator().next();

        Assert.assertNotNull(result);
        List<String> actualIds = result.getValue().stream().map(s -> (String) s.getDocument().get("HotelId")).collect(Collectors.toList());
        List<String> expectedIds = Arrays.asList("1", "9", "4", "3", "5");
        Assert.assertEquals(expectedIds, actualIds);
    }
}
