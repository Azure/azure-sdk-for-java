// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.data.customization;

import com.azure.core.http.rest.PagedFlux;
import com.azure.search.data.SearchIndexAsyncClient;
import com.azure.search.data.generated.models.SuggestParameters;
import com.azure.search.data.generated.models.SuggestResult;

import org.junit.Assert;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.azure.search.data.customization.SearchTestBase.HOTELS_DATA_JSON;
import static com.azure.search.data.customization.SearchTestBase.HOTELS_INDEX_NAME;

public class SuggestAsyncTests extends SuggestTestBase {

    private SearchIndexAsyncClient client;

    @Override
    protected void initializeClient() {
        client = builderSetup().indexName(HOTELS_INDEX_NAME).buildAsyncClient();
    }

    @Override
    public void canSuggestDynamicDocuments() {
        uploadDocumentsJson(client, HOTELS_INDEX_NAME, HOTELS_DATA_JSON);
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
        uploadDocumentsJson(client, HOTELS_INDEX_NAME, HOTELS_DATA_JSON);
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
        uploadDocumentsJson(client, HOTELS_INDEX_NAME, HOTELS_DATA_JSON);
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
        uploadDocumentsJson(client, HOTELS_INDEX_NAME, HOTELS_DATA_JSON);
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
        List<Map<String, Object>> hotels = uploadDocumentsJson(client, HOTELS_INDEX_NAME, HOTELS_DATA_JSON);
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
    public void canSuggestWithDateTimeInStaticModel() {
    }

    @Override
    public void fuzzyIsOffByDefault() {
        uploadDocumentsJson(client, HOTELS_INDEX_NAME, HOTELS_DATA_JSON);

        PagedFlux<SuggestResult> suggestResult = client.suggest("hitel", "sg", null, null);

        StepVerifier
            .create(suggestResult.byPage())
            .assertNext(this::verifyFuzzyIsOffByDefault)
            .verifyComplete();
    }

    @Override
    public void suggestThrowsWhenGivenBadSuggesterName() {
        PagedFlux<SuggestResult> suggestResult = client.suggest("Hotel", "Suggester does not exist", null, null);

        StepVerifier
            .create(suggestResult.byPage())
            .verifyErrorSatisfies(this::verifySuggestThrowsWhenGivenBadSuggesterName);
    }

    @Override
    public void suggestThrowsWhenRequestIsMalformed() {
        uploadDocumentsJson(client, HOTELS_INDEX_NAME, HOTELS_DATA_JSON);
        SuggestParameters suggestParams = new SuggestParameters()
            .orderBy(Collections.singletonList("This is not a valid orderby."));

        PagedFlux<SuggestResult> suggestResult = client.suggest("hotel", "sg", suggestParams, null);

        StepVerifier
            .create(suggestResult.byPage())
            .verifyErrorSatisfies(this::verifySuggestThrowsWhenRequestIsMalformed);
    }

    @Override
    public void testCanSuggestWithMinimumCoverage() {
        uploadDocumentsJson(client, HOTELS_INDEX_NAME, HOTELS_DATA_JSON);

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
        uploadDocumentsJson(client, HOTELS_INDEX_NAME, HOTELS_DATA_JSON);

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
        uploadDocumentsJson(client, HOTELS_INDEX_NAME, HOTELS_DATA_JSON);

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
        uploadDocumentsJson(client, HOTELS_INDEX_NAME, HOTELS_DATA_JSON);

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
