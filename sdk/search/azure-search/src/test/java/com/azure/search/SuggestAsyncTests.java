// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import com.azure.core.http.rest.PagedFlux;
import com.azure.search.models.SuggestParameters;
import com.azure.search.models.SuggestResult;
import com.azure.search.test.environment.models.Author;
import com.azure.search.test.environment.models.Book;
import org.junit.Assert;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.azure.search.SearchTestBase.HOTELS_DATA_JSON;
import static com.azure.search.SearchTestBase.HOTELS_INDEX_NAME;

public class SuggestAsyncTests extends SuggestTestBase {

    private SearchIndexAsyncClient client;

    @Override
    public void canSuggestDynamicDocuments() {
        createHotelIndex();
        client = getClientBuilder(HOTELS_INDEX_NAME).buildAsyncClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);
        SuggestParameters suggestParams = new SuggestParameters()
            .orderBy(Collections.singletonList("HotelId"));
        PagedFlux<SuggestResult> suggestResult = client.suggest("more", "sg", suggestParams, null);

        StepVerifier
            .create(suggestResult.byPage())
            .assertNext(this::verifyDynamicDocumentSuggest)
            .verifyComplete();
    }

    @Override
    public void searchFieldsExcludesFieldsFromSuggest() {
        createHotelIndex();
        client = getClientBuilder(HOTELS_INDEX_NAME).buildAsyncClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);
        SuggestParameters suggestParams = new SuggestParameters()
            .searchFields(new LinkedList<>(Collections.singletonList("HotelName")));

        PagedFlux<SuggestResult> suggestResult = client.suggest("luxury", "sg", suggestParams, null);

        StepVerifier
            .create(suggestResult.byPage())
            .assertNext(this::verifyFieldsExcludesFieldsSuggest)
            .verifyComplete();
    }

    @Override
    public void canUseSuggestHitHighlighting() {
        createHotelIndex();
        client = getClientBuilder(HOTELS_INDEX_NAME).buildAsyncClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);
        SuggestParameters suggestParams = new SuggestParameters()
            .highlightPreTag("<b>")
            .highlightPostTag("</b>")
            .filter("Category eq 'Luxury'")
            .top(1);

        PagedFlux<SuggestResult> suggestResult = client.suggest("hotel", "sg", suggestParams, null);

        StepVerifier
            .create(suggestResult.byPage())
            .assertNext(this::verifyHitHighlightingSuggest)
            .verifyComplete();
    }

    @Override
    public void canGetFuzzySuggestions() {
        createHotelIndex();
        client = getClientBuilder(HOTELS_INDEX_NAME).buildAsyncClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);
        SuggestParameters suggestParams = new SuggestParameters()
            .useFuzzyMatching(true);

        PagedFlux<SuggestResult> suggestResult = client.suggest("hitel", "sg", suggestParams, null);

        StepVerifier
            .create(suggestResult.byPage())
            .assertNext(this::verifyFuzzySuggest)
            .verifyComplete();
    }

    @Override
    public void canSuggestStaticallyTypedDocuments() {
        createHotelIndex();
        client = getClientBuilder(HOTELS_INDEX_NAME).buildAsyncClient();

        List<Map<String, Object>> hotels = uploadDocumentsJson(client, HOTELS_DATA_JSON);
        //arrange
        SuggestParameters suggestParams = new SuggestParameters()
            .orderBy(new LinkedList<>(Collections.singletonList("HotelId")));

        //act
        PagedFlux<SuggestResult> suggestResult = client.suggest("more", "sg", suggestParams, null);

        StepVerifier
            .create(suggestResult.byPage())
            .assertNext(result -> verifyCanSuggestStaticallyTypedDocuments(result, hotels))
            .verifyComplete();
    }

    @Override
    public void canSuggestWithDateTimeInStaticModel() throws Exception {
        setupIndexFromJsonFile(BOOKS_INDEX_JSON);
        client = getClientBuilder(BOOKS_INDEX_NAME).buildAsyncClient();
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

        SuggestParameters suggestParams = new SuggestParameters();
        suggestParams.select(Arrays.asList("ISBN", "Title", "PublishDate"));
        PagedFlux<SuggestResult> suggestResult = client.suggest("War", "sg", suggestParams, null);

        StepVerifier
            .create(suggestResult.byPage())
            .assertNext(this::verifyCanSuggestWithDateTimeInStaticModel)
            .verifyComplete();
    }

    @Override
    public void fuzzyIsOffByDefault() {
        createHotelIndex();
        client = getClientBuilder(HOTELS_INDEX_NAME).buildAsyncClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);

        PagedFlux<SuggestResult> suggestResult = client.suggest("hitel", "sg", null, null);

        StepVerifier
            .create(suggestResult.byPage())
            .assertNext(this::verifyFuzzyIsOffByDefault)
            .verifyComplete();
    }

    @Override
    public void suggestThrowsWhenGivenBadSuggesterName() {
        createHotelIndex();
        client = getClientBuilder(HOTELS_INDEX_NAME).buildAsyncClient();

        PagedFlux<SuggestResult> suggestResult = client.suggest("Hotel", "Suggester does not exist", null, null);

        StepVerifier
            .create(suggestResult.byPage())
            .verifyErrorSatisfies(this::verifySuggestThrowsWhenGivenBadSuggesterName);
    }

    @Override
    public void suggestThrowsWhenRequestIsMalformed() {
        createHotelIndex();
        client = getClientBuilder(HOTELS_INDEX_NAME).buildAsyncClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);
        SuggestParameters suggestParams = new SuggestParameters()
            .orderBy(Collections.singletonList("This is not a valid orderby."));

        PagedFlux<SuggestResult> suggestResult = client.suggest("hotel", "sg", suggestParams, null);

        StepVerifier
            .create(suggestResult.byPage())
            .verifyErrorSatisfies(this::verifySuggestThrowsWhenRequestIsMalformed);
    }

    @Override
    public void testCanSuggestWithMinimumCoverage() {
        createHotelIndex();
        client = getClientBuilder(HOTELS_INDEX_NAME).buildAsyncClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);

        //arrange
        SuggestParameters suggestParams = new SuggestParameters()
            .orderBy(new LinkedList<>(Collections.singletonList("HotelId")))
            .minimumCoverage(50.0);

        //act
        PagedFlux<SuggestResult> suggestResult = client.suggest("luxury", "sg", suggestParams, null);

        StepVerifier
            .create(suggestResult.byPage())
            .assertNext(this::verifyMinimumCoverage)
            .verifyComplete();
    }

    @Override
    public void testTopTrimsResults() {
        createHotelIndex();
        client = getClientBuilder(HOTELS_INDEX_NAME).buildAsyncClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);

        //arrange
        SuggestParameters suggestParams = new SuggestParameters();
        suggestParams.orderBy(Collections.singletonList("HotelId"));
        suggestParams.top(3);

        //act
        PagedFlux<SuggestResult> suggestResult = client.suggest("hotel", "sg", suggestParams, null);

        StepVerifier
            .create(suggestResult.byPage())
            .assertNext(this::verifyTopDocumentSuggest)
            .verifyComplete();
    }

    @Override
    public void testCanFilter() {
        createHotelIndex();
        client = getClientBuilder(HOTELS_INDEX_NAME).buildAsyncClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);

        SuggestParameters suggestParams = new SuggestParameters()
            .filter("Rating gt 3 and LastRenovationDate gt 2000-01-01T00:00:00Z")
            .orderBy(Arrays.asList("HotelId"));

        PagedFlux<SuggestResult> suggestResult = client.suggest("hotel", "sg", suggestParams, null);

        StepVerifier.create(suggestResult.byPage())
            .assertNext(nextPage -> {
                List<String> actualIds = nextPage.value().stream().map(s -> (String) s.additionalProperties().get("HotelId")).collect(Collectors.toList());
                List<String> expectedIds = Arrays.asList("1", "5");
                Assert.assertEquals(expectedIds, actualIds);
            })
            .verifyComplete();
    }

    @Override
    public void testOrderByProgressivelyBreaksTies() {
        createHotelIndex();
        client = getClientBuilder(HOTELS_INDEX_NAME).buildAsyncClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);

        SuggestParameters suggestParams = new SuggestParameters()
            .orderBy(Arrays.asList("Rating desc",
                "LastRenovationDate asc",
                "geo.distance(Location, geography'POINT(-122.0 49.0)')"));

        PagedFlux<SuggestResult> suggestResult = client.suggest("hotel", "sg", suggestParams, null);

        StepVerifier
            .create(suggestResult.byPage())
            .assertNext(nextPage -> {
                List<String> actualIds = nextPage.value().stream().map(s -> (String) s.additionalProperties().get("HotelId")).collect(Collectors.toList());
                List<String> expectedIds = Arrays.asList("1", "9", "4", "3", "5");
                Assert.assertEquals(expectedIds, actualIds);
            })
            .verifyComplete();
    }
}
