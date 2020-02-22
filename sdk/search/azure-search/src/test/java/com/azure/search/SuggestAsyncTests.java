// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import com.azure.core.http.rest.PagedFluxBase;
import com.azure.search.models.SuggestOptions;
import com.azure.search.models.SuggestResult;
import com.azure.search.test.environment.models.Author;
import com.azure.search.test.environment.models.Book;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.azure.search.SearchTestBase.HOTELS_DATA_JSON;
import static com.azure.search.SearchTestBase.HOTELS_INDEX_NAME;

public class SuggestAsyncTests extends SuggestTestBase {

    private SearchIndexAsyncClient client;

    @Override
    public void canSuggestDynamicDocuments() throws IOException {
        createHotelIndex();
        client = getSearchIndexClientBuilder(HOTELS_INDEX_NAME).buildAsyncClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);
        SuggestOptions suggestOptions = new SuggestOptions()
            .setOrderBy("HotelId");
        PagedFluxBase<SuggestResult, SuggestPagedResponse> suggestResult = client.suggest("more", "sg", suggestOptions, generateRequestOptions());

        StepVerifier
            .create(suggestResult.byPage())
            .assertNext(this::verifyDynamicDocumentSuggest)
            .verifyComplete();
    }

    @Test
    public void searchFieldsExcludesFieldsFromSuggest() throws IOException {
        createHotelIndex();
        client = getSearchIndexClientBuilder(HOTELS_INDEX_NAME).buildAsyncClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);
        SuggestOptions suggestOptions = new SuggestOptions()
            .setSearchFields("HotelName");

        PagedFluxBase<SuggestResult, SuggestPagedResponse> suggestResult = client.suggest("luxury", "sg", suggestOptions, generateRequestOptions());

        StepVerifier
            .create(suggestResult.byPage())
            .assertNext(this::verifyFieldsExcludesFieldsSuggest)
            .verifyComplete();
    }

    @Test
    public void canUseSuggestHitHighlighting() throws IOException {
        createHotelIndex();
        client = getSearchIndexClientBuilder(HOTELS_INDEX_NAME).buildAsyncClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);
        SuggestOptions suggestOptions = new SuggestOptions()
            .setHighlightPreTag("<b>")
            .setHighlightPostTag("</b>")
            .setFilter("Category eq 'Luxury'")
            .setTop(1);

        PagedFluxBase<SuggestResult, SuggestPagedResponse> suggestResult = client.suggest("hotel", "sg", suggestOptions, generateRequestOptions());

        StepVerifier
            .create(suggestResult.byPage())
            .assertNext(this::verifyHitHighlightingSuggest)
            .verifyComplete();
    }

    @Test
    public void canGetFuzzySuggestions() throws IOException {
        createHotelIndex();
        client = getSearchIndexClientBuilder(HOTELS_INDEX_NAME).buildAsyncClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);
        SuggestOptions suggestOptions = new SuggestOptions()
            .setUseFuzzyMatching(true);

        PagedFluxBase<SuggestResult, SuggestPagedResponse> suggestResult = client.suggest("hitel", "sg", suggestOptions, generateRequestOptions());

        StepVerifier
            .create(suggestResult.byPage())
            .assertNext(this::verifyFuzzySuggest)
            .verifyComplete();
    }

    @Test
    public void canSuggestStaticallyTypedDocuments() throws IOException {
        createHotelIndex();
        client = getSearchIndexClientBuilder(HOTELS_INDEX_NAME).buildAsyncClient();

        List<Map<String, Object>> hotels = uploadDocumentsJson(client, HOTELS_DATA_JSON);
        //arrange
        SuggestOptions suggestOptions = new SuggestOptions()
            .setOrderBy("HotelId");

        //act
        PagedFluxBase<SuggestResult, SuggestPagedResponse> suggestResult = client.suggest("more", "sg", suggestOptions, generateRequestOptions());

        StepVerifier
            .create(suggestResult.byPage())
            .assertNext(result -> verifyCanSuggestStaticallyTypedDocuments(result, hotels))
            .verifyComplete();
    }

    @Test
    public void canSuggestWithDateTimeInStaticModel() {
        setupIndexFromJsonFile(BOOKS_INDEX_JSON);
        client = getSearchIndexClientBuilder(BOOKS_INDEX_NAME).buildAsyncClient();
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
        PagedFluxBase<SuggestResult, SuggestPagedResponse> suggestResult = client.suggest("War", "sg", suggestOptions, generateRequestOptions());

        StepVerifier
            .create(suggestResult.byPage())
            .assertNext(this::verifyCanSuggestWithDateTimeInStaticModel)
            .verifyComplete();
    }

    @Test
    public void fuzzyIsOffByDefault() throws IOException {
        createHotelIndex();
        client = getSearchIndexClientBuilder(HOTELS_INDEX_NAME).buildAsyncClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);

        PagedFluxBase<SuggestResult, SuggestPagedResponse> suggestResult = client.suggest("hitel", "sg", null, generateRequestOptions());

        StepVerifier
            .create(suggestResult.byPage())
            .assertNext(this::verifyFuzzyIsOffByDefault)
            .verifyComplete();

        PagedFluxBase<SuggestResult, SuggestPagedResponse> suggestResultWithoutSuggestOptions = client.suggest(
            "hitel", "sg");

        StepVerifier
            .create(suggestResultWithoutSuggestOptions.byPage())
            .assertNext(this::verifyFuzzyIsOffByDefault)
            .verifyComplete();
    }

    @Test
    public void suggestThrowsWhenGivenBadSuggesterName() {
        createHotelIndex();
        client = getSearchIndexClientBuilder(HOTELS_INDEX_NAME).buildAsyncClient();

        assertHttpResponseExceptionAsync(
            client.suggest(
                "Hotel", "Suggester does not exist", new SuggestOptions(), generateRequestOptions()),
            HttpResponseStatus.BAD_REQUEST,
            "The specified suggester name 'Suggester does not exist' does not exist in this index definition."
        );
    }

    @Test
    public void suggestThrowsWhenRequestIsMalformed() {
        createHotelIndex();
        client = getSearchIndexClientBuilder(HOTELS_INDEX_NAME).buildAsyncClient();

        SuggestOptions suggestOptions = new SuggestOptions()
            .setOrderBy("This is not a valid orderby.");

        assertHttpResponseExceptionAsync(
            client.suggest("hotel", "sg", suggestOptions, generateRequestOptions()),
            HttpResponseStatus.BAD_REQUEST,
            "Invalid expression: Syntax error at position 7 in 'This is not a valid orderby.'"
        );
    }

    @Test
    public void testCanSuggestWithMinimumCoverage() throws IOException {
        createHotelIndex();
        client = getSearchIndexClientBuilder(HOTELS_INDEX_NAME).buildAsyncClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);

        //arrange
        SuggestOptions suggestOptions = new SuggestOptions()
            .setOrderBy("HotelId")
            .setMinimumCoverage(50.0);

        //act
        PagedFluxBase<SuggestResult, SuggestPagedResponse> suggestResult = client.suggest("luxury", "sg", suggestOptions, generateRequestOptions());

        StepVerifier
            .create(suggestResult.byPage())
            .assertNext(this::verifyMinimumCoverage)
            .verifyComplete();
    }

    @Test
    public void testTopTrimsResults() throws IOException {
        createHotelIndex();
        client = getSearchIndexClientBuilder(HOTELS_INDEX_NAME).buildAsyncClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);

        //arrange
        SuggestOptions suggestOptions = new SuggestOptions();
        suggestOptions.setOrderBy("HotelId");
        suggestOptions.setTop(3);

        //act
        PagedFluxBase<SuggestResult, SuggestPagedResponse> suggestResult = client.suggest("hotel", "sg", suggestOptions, generateRequestOptions());

        StepVerifier
            .create(suggestResult.byPage())
            .assertNext(this::verifyTopDocumentSuggest)
            .verifyComplete();
    }

    @Test
    public void testCanFilter() throws IOException {
        createHotelIndex();
        client = getSearchIndexClientBuilder(HOTELS_INDEX_NAME).buildAsyncClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);

        SuggestOptions suggestOptions = new SuggestOptions()
            .setFilter("Rating gt 3 and LastRenovationDate gt 2000-01-01T00:00:00Z")
            .setOrderBy("HotelId");
        PagedFluxBase<SuggestResult, SuggestPagedResponse> suggestResult = client.suggest("hotel", "sg", suggestOptions, generateRequestOptions());

        StepVerifier.create(suggestResult.byPage())
            .assertNext(nextPage -> {
                List<String> actualIds = nextPage.getValue().stream().map(s -> (String) s.getDocument().get("HotelId")).collect(Collectors.toList());
                List<String> expectedIds = Arrays.asList("1", "5");
                Assert.assertEquals(expectedIds, actualIds);
            })
            .verifyComplete();
    }

    @Test
    public void testOrderByProgressivelyBreaksTies() throws IOException {
        createHotelIndex();
        client = getSearchIndexClientBuilder(HOTELS_INDEX_NAME).buildAsyncClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);

        SuggestOptions suggestOptions = new SuggestOptions()
            .setOrderBy("Rating desc",
                "LastRenovationDate asc",
                "geo.distance(Location, geography'POINT(-122.0 49.0)')");

        PagedFluxBase<SuggestResult, SuggestPagedResponse>suggestResult = client.suggest("hotel", "sg", suggestOptions, generateRequestOptions());

        StepVerifier
            .create(suggestResult.byPage())
            .assertNext(nextPage -> {
                List<String> actualIds = nextPage.getValue().stream().map(s -> (String) s.getDocument().get("HotelId")).collect(Collectors.toList());
                List<String> expectedIds = Arrays.asList("1", "9", "4", "3", "5");
                Assert.assertEquals(expectedIds, actualIds);
            })
            .verifyComplete();
    }

    @Test
    public void testCanSuggestWithSelectedFields() throws IOException {
        createHotelIndex();
        client = getClientBuilder(HOTELS_INDEX_NAME).buildAsyncClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);

        SuggestOptions suggestOptions = new SuggestOptions()
            .setSelect("HotelName", "Rating", "Address/City", "Rooms/Type");
        PagedFluxBase<SuggestResult, SuggestPagedResponse> suggestResult = client.suggest("secret", "sg", suggestOptions, generateRequestOptions());

        StepVerifier
            .create(suggestResult.byPage())
            .assertNext(this::verifySuggestWithSelectedFields)
            .verifyComplete();



    }
}
