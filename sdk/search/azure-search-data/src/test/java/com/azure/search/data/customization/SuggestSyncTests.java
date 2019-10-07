// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.data.customization;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.search.data.SearchIndexClient;
import com.azure.search.data.generated.models.SuggestParameters;
import com.azure.search.data.generated.models.SuggestResult;
import com.azure.search.test.environment.models.Author;
import com.azure.search.test.environment.models.Book;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.*;
import java.util.stream.Collectors;

import static com.azure.search.data.customization.SearchTestBase.HOTELS_DATA_JSON;
import static com.azure.search.data.customization.SearchTestBase.HOTELS_INDEX_NAME;

public class SuggestSyncTests extends SuggestTestBase {

    private SearchIndexClient client;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Override
    protected void initializeClient() {
        client = builderSetup().indexName(HOTELS_INDEX_NAME).buildClient();
    }

    @Test
    public void canSuggestDynamicDocuments() {
        uploadDocumentsJson(client, HOTELS_INDEX_NAME, HOTELS_DATA_JSON);
        SuggestParameters suggestParams = new SuggestParameters()
            .orderBy(Collections.singletonList("HotelId"));

        PagedIterable<SuggestResult> suggestResult = client.suggest("more", "sg", suggestParams, null);
        Iterator<PagedResponse<SuggestResult>> iterator = suggestResult.iterableByPage().iterator();

        verifyDynamicDocumentSuggest(iterator.next());
        Assert.assertFalse(iterator.hasNext());
    }

    @Test
    public void searchFieldsExcludesFieldsFromSuggest() {
        uploadDocumentsJson(client, HOTELS_INDEX_NAME, HOTELS_DATA_JSON);
        SuggestParameters suggestParams = new SuggestParameters()
            .searchFields(Collections.singletonList("HotelName"));

        PagedIterable<SuggestResult> suggestResult = client.suggest("luxury", "sg", suggestParams, null);
        Iterator<PagedResponse<SuggestResult>> iterator = suggestResult.iterableByPage().iterator();

        verifyFieldsExcludesFieldsSuggest(iterator.next());
        Assert.assertFalse(iterator.hasNext());
    }

    @Test
    public void canUseSuggestHitHighlighting() {
        uploadDocumentsJson(client, HOTELS_INDEX_NAME, HOTELS_DATA_JSON);
        SuggestParameters suggestParams = new SuggestParameters()
            .highlightPreTag("<b>")
            .highlightPostTag("</b>")
            .filter("Category eq 'Luxury'")
            .top(1);

        PagedIterable<SuggestResult> suggestResult = client.suggest("hotel", "sg", suggestParams, null);
        Iterator<PagedResponse<SuggestResult>> iterator = suggestResult.iterableByPage().iterator();

        verifyHitHighlightingSuggest(iterator.next());
        Assert.assertFalse(iterator.hasNext());
    }

    @Test
    public void canGetFuzzySuggestions() {
        uploadDocumentsJson(client, HOTELS_INDEX_NAME, HOTELS_DATA_JSON);
        SuggestParameters suggestParams = new SuggestParameters()
            .useFuzzyMatching(true);

        PagedIterable<SuggestResult> suggestResult = client.suggest("hitel", "sg", suggestParams, null);
        Iterator<PagedResponse<SuggestResult>> iterator = suggestResult.iterableByPage().iterator();

        verifyFuzzySuggest(iterator.next());
        Assert.assertFalse(iterator.hasNext());
    }

    @Override
    public void canSuggestStaticallyTypedDocuments() {
        List<Map<String, Object>> hotels = uploadDocumentsJson(client, HOTELS_INDEX_NAME, HOTELS_DATA_JSON);
        //arrange
        SuggestParameters suggestParams = new SuggestParameters()
            .orderBy(Collections.singletonList("HotelId"));

        //act
        PagedIterable<SuggestResult> suggestResult = client.suggest("more", "sg", suggestParams, null);
        Iterator<PagedResponse<SuggestResult>> iterator = suggestResult.iterableByPage().iterator();

        //assert
        verifyCanSuggestStaticallyTypedDocuments(iterator.next(), hotels);
    }

    @Override
    public void canSuggestWithDateTimeInStaticModel() throws Exception {
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
        doc2.publishDate(DATE_FORMAT.parse("2015-08-18T00:00:00Z"));
        uploadDocuments(client, BOOKS_INDEX_NAME, Arrays.asList(doc1, doc2));

        SuggestParameters suggestParams = new SuggestParameters();
        suggestParams.select(Arrays.asList("ISBN", "Title", "PublishDate"));
        PagedIterable<SuggestResult> suggestResult = client.suggest("War", "sg", suggestParams, null);
        Iterator<PagedResponse<SuggestResult>> iterator = suggestResult.iterableByPage().iterator();

        //assert
        verifyCanSuggestWithDateTimeInStaticModel(iterator.next());
    }

    @Override
    public void fuzzyIsOffByDefault() {
        uploadDocumentsJson(client, HOTELS_INDEX_NAME, HOTELS_DATA_JSON);

        PagedIterable<SuggestResult> suggestResult = client.suggest("hitel", "sg", null, null);
        Iterator<PagedResponse<SuggestResult>> iterator = suggestResult.iterableByPage().iterator();

        verifyFuzzyIsOffByDefault(iterator.next());
    }

    @Override
    public void suggestThrowsWhenGivenBadSuggesterName() {
        thrown.expect(HttpResponseException.class);
        thrown.expectMessage("The specified suggester name 'Suggester does not exist' "
            + "does not exist in this index definition.");

        PagedIterable<SuggestResult> suggestResult = client.suggest("Hotel", "Suggester does not exist", null, null);
        suggestResult.iterableByPage().iterator().next();
    }

    @Override
    public void suggestThrowsWhenRequestIsMalformed() {
        thrown.expect(HttpResponseException.class);
        thrown.expectMessage("Invalid expression: Syntax error at position 7 in 'This is not a valid orderby.'");

        uploadDocumentsJson(client, HOTELS_INDEX_NAME, HOTELS_DATA_JSON);
        SuggestParameters suggestParams = new SuggestParameters()
            .orderBy(new LinkedList<>(Collections.singletonList("This is not a valid orderby.")));

        PagedIterable<SuggestResult> suggestResult = client.suggest("hotel", "sg", suggestParams, null);
        suggestResult.iterableByPage().iterator().next();
    }

    @Override
    public void testCanSuggestWithMinimumCoverage() {
        uploadDocumentsJson(client, HOTELS_INDEX_NAME, HOTELS_DATA_JSON);

        //arrange
        SuggestParameters suggestParams = new SuggestParameters()
            .orderBy(new LinkedList<>(Collections.singletonList("HotelId")))
            .minimumCoverage(50.0);

        //act
        PagedResponse<SuggestResult> suggestResult = client
            .suggest("luxury", "sg", suggestParams, null)
            .iterableByPage()
            .iterator()
            .next();

        verifyMinimumCoverage(suggestResult);

    }

    @Override
    public void testTopTrimsResults() {
        uploadDocumentsJson(client, HOTELS_INDEX_NAME, HOTELS_DATA_JSON);
        //arrange
        SuggestParameters suggestParams = new SuggestParameters()
            .orderBy(Collections.singletonList("HotelId"))
            .top(3);

        //act
        PagedIterable<SuggestResult> suggestResult = client.suggest("more",
            "sg",
            suggestParams,
            null);
        Iterator<PagedResponse<SuggestResult>> iterator = suggestResult.iterableByPage().iterator();

        //assert
        verifyTopDocumentSuggest(iterator.next());
    }

    @Override
    public void testCanFilter() {
        uploadDocumentsJson(client, HOTELS_INDEX_NAME, HOTELS_DATA_JSON);

        SuggestParameters suggestParams = new SuggestParameters()
            .filter("Rating gt 3 and LastRenovationDate gt 2000-01-01T00:00:00Z")
            .orderBy(Arrays.asList("HotelId"));

        PagedIterable<SuggestResult> suggestResult = client.suggest("hotel", "sg", suggestParams, null);
        PagedResponse<SuggestResult> result = suggestResult.iterableByPage().iterator().next();

        Assert.assertNotNull(result);
        List<String> actualIds = result.value().stream().map(s -> (String) s.additionalProperties().get("HotelId")).collect(Collectors.toList());
        List<String> expectedIds = Arrays.asList("1", "5");
        Assert.assertEquals(expectedIds, actualIds);
    }

    @Override
    public void testOrderByProgressivelyBreaksTies() {
        uploadDocumentsJson(client, HOTELS_INDEX_NAME, HOTELS_DATA_JSON);

        SuggestParameters suggestParams = new SuggestParameters()
            .orderBy(Arrays.asList("Rating desc",
                "LastRenovationDate asc",
                "geo.distance(Location, geography'POINT(-122.0 49.0)')"));

        PagedIterable<SuggestResult> suggestResult = client.suggest("hotel", "sg", suggestParams, null);
        PagedResponse<SuggestResult> result = suggestResult.iterableByPage().iterator().next();

        Assert.assertNotNull(result);
        List<String> actualIds = result.value().stream().map(s -> (String) s.additionalProperties().get("HotelId")).collect(Collectors.toList());
        List<String> expectedIds = Arrays.asList("1", "9", "4", "3", "5");
        Assert.assertEquals(expectedIds, actualIds);
    }
}
