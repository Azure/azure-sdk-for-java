// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.data.customization;

import com.azure.core.http.rest.PagedFlux;
import com.azure.search.data.SearchIndexAsyncClient;
import com.azure.search.data.generated.models.SuggestParameters;
import com.azure.search.data.generated.models.SuggestResult;
import reactor.test.StepVerifier;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
        SuggestParameters suggestParams = new SuggestParameters();
        suggestParams.orderBy(new LinkedList<>(Collections.singletonList("HotelId")));
        PagedFlux<SuggestResult> suggestResult = client.suggest("more", "sg", suggestParams, null);

        StepVerifier
            .create(suggestResult.byPage())
            .assertNext(this::verifyDynamicDocumentSuggest)
            .verifyComplete();
    }

    @Override
    public void searchFieldsExcludesFieldsFromSuggest() {
        uploadDocumentsJson(client, HOTELS_INDEX_NAME, HOTELS_DATA_JSON);
        SuggestParameters suggestParams = new SuggestParameters();
        suggestParams.searchFields(new LinkedList<>(Collections.singletonList("HotelName")));

        PagedFlux<SuggestResult> suggestResult = client.suggest("luxury", "sg", suggestParams, null);

        StepVerifier
            .create(suggestResult.byPage())
            .assertNext(this::verifyFieldsExcludesFieldsSuggest)
            .verifyComplete();
    }

    @Override
    public void canUseSuggestHitHighlighting() {
        uploadDocumentsJson(client, HOTELS_INDEX_NAME, HOTELS_DATA_JSON);
        SuggestParameters suggestParams = new SuggestParameters();
        suggestParams.highlightPreTag("<b>");
        suggestParams.highlightPostTag("</b>");
        suggestParams.filter("Category eq 'Luxury'");
        suggestParams.top(1);

        PagedFlux<SuggestResult> suggestResult = client.suggest("hotel", "sg", suggestParams, null);

        StepVerifier
            .create(suggestResult.byPage())
            .assertNext(this::verifyHitHighlightingSuggest)
            .verifyComplete();
    }

    @Override
    public void canGetFuzzySuggestions() {
        uploadDocumentsJson(client, HOTELS_INDEX_NAME, HOTELS_DATA_JSON);
        SuggestParameters suggestParams = new SuggestParameters();
        suggestParams.useFuzzyMatching(true);

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
        SuggestParameters suggestParams = new SuggestParameters();
        suggestParams.orderBy(new LinkedList<>(Collections.singletonList("HotelId")));

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
        SuggestParameters suggestParams = new SuggestParameters();
        suggestParams.orderBy(new LinkedList<>(Collections.singletonList("This is not a valid orderby.")));
        PagedFlux<SuggestResult> suggestResult = client.suggest("hotel", "sg", suggestParams, null);

        StepVerifier
            .create(suggestResult.byPage())
            .verifyErrorSatisfies(this::verifySuggestThrowsWhenRequestIsMalformed);
    }


    @Override
    public void testTopTrimsResults() {
        uploadDocumentsJson(client, HOTELS_INDEX_NAME, HOTELS_DATA_JSON);

        //arrange
        SuggestParameters suggestParams = new SuggestParameters();
        suggestParams.orderBy(new LinkedList<>(Collections.singletonList("HotelId")));
        suggestParams.top(3);

        //act
        PagedFlux<SuggestResult> suggestResult = client.suggest("hotel", "sg", suggestParams, null);

        StepVerifier
            .create(suggestResult.byPage())
            .assertNext(this::verifyTopDocumentSuggest)
            .verifyComplete();
    }
}
