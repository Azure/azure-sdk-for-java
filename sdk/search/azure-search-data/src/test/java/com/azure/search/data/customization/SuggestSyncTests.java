// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.data.customization;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.search.data.SearchIndexClient;
import com.azure.search.data.generated.models.SuggestParameters;
import com.azure.search.data.generated.models.SuggestResult;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
        SuggestParameters suggestParams = new SuggestParameters();
        suggestParams.orderBy(new LinkedList<>(Collections.singletonList("HotelId")));

        PagedIterable<SuggestResult> suggestResult = client.suggest("more",
            "sg",
            suggestParams,
            null);
        Iterator<PagedResponse<SuggestResult>> iterator = suggestResult.iterableByPage().iterator();

        verifyDynamicDocumentSuggest(iterator.next());
        Assert.assertFalse(iterator.hasNext());
    }

    @Test
    public void searchFieldsExcludesFieldsFromSuggest() {
        uploadDocumentsJson(client, HOTELS_INDEX_NAME, HOTELS_DATA_JSON);
        SuggestParameters suggestParams = new SuggestParameters();
        suggestParams.searchFields(new LinkedList<>(Collections.singletonList("HotelName")));

        PagedIterable<SuggestResult> suggestResult = client.suggest("luxury",
            "sg",
            suggestParams,
            null);
        Iterator<PagedResponse<SuggestResult>> iterator = suggestResult.iterableByPage().iterator();

        verifyFieldsExcludesFieldsSuggest(iterator.next());
        Assert.assertFalse(iterator.hasNext());
    }

    @Test
    public void canUseSuggestHitHighlighting() {
        uploadDocumentsJson(client, HOTELS_INDEX_NAME, HOTELS_DATA_JSON);
        SuggestParameters suggestParams = new SuggestParameters();
        suggestParams.highlightPreTag("<b>");
        suggestParams.highlightPostTag("</b>");
        suggestParams.filter("Category eq 'Luxury'");
        suggestParams.top(1);

        PagedIterable<SuggestResult> suggestResult = client.suggest("hotel",
            "sg",
            suggestParams,
            null);
        Iterator<PagedResponse<SuggestResult>> iterator = suggestResult.iterableByPage().iterator();

        verifyHitHighlightingSuggest(iterator.next());
        Assert.assertFalse(iterator.hasNext());
    }

    @Test
    public void canGetFuzzySuggestions() {
        uploadDocumentsJson(client, HOTELS_INDEX_NAME, HOTELS_DATA_JSON);
        SuggestParameters suggestParams = new SuggestParameters();
        suggestParams.useFuzzyMatching(true);

        PagedIterable<SuggestResult> suggestResult = client.suggest("hitel",
            "sg",
            suggestParams,
            null);
        Iterator<PagedResponse<SuggestResult>> iterator = suggestResult.iterableByPage().iterator();

        verifyFuzzySuggest(iterator.next());
        Assert.assertFalse(iterator.hasNext());
    }

    @Override
    public void canSuggestStaticallyTypedDocuments() {
        List<Map<String, Object>> hotels = uploadDocumentsJson(client, HOTELS_INDEX_NAME, HOTELS_DATA_JSON);
        //arrange
        SuggestParameters suggestParams = new SuggestParameters();
        suggestParams.orderBy(new LinkedList<>(Collections.singletonList("HotelId")));

        //act
        PagedIterable<SuggestResult> suggestResult = client.suggest("more",
            "sg",
            suggestParams,
            null);
        Iterator<PagedResponse<SuggestResult>> iterator = suggestResult.iterableByPage().iterator();

        //assert
        verifyCanSuggestStaticallyTypedDocuments(iterator.next(), hotels);
    }

    @Override
    public void canSuggestWithDateTimeInStaticModel() {
    }

    @Override
    public void fuzzyIsOffByDefault() {
        uploadDocumentsJson(client, HOTELS_INDEX_NAME, HOTELS_DATA_JSON);

        PagedIterable<SuggestResult> suggestResult = client.suggest("hitel",
            "sg",
            null,
            null);
        Iterator<PagedResponse<SuggestResult>> iterator = suggestResult.iterableByPage().iterator();

        verifyFuzzyIsOffByDefault(iterator.next());
    }

    @Override
    public void suggestThrowsWhenGivenBadSuggesterName() {
        thrown.expect(HttpResponseException.class);
        thrown.expectMessage("The specified suggester name 'Suggester does not exist' "
            + "does not exist in this index definition.");

        PagedIterable<SuggestResult> suggestResult = client.suggest("Hotel",
            "Suggester does not exist",
            null,
            null);
        suggestResult.iterableByPage().iterator().next();
    }

    @Override
    public void suggestThrowsWhenRequestIsMalformed() {
        thrown.expect(HttpResponseException.class);
        thrown.expectMessage("Invalid expression: "
            + "Syntax error at position 7 in 'This is not a valid orderby.'");

        uploadDocumentsJson(client, HOTELS_INDEX_NAME, HOTELS_DATA_JSON);
        SuggestParameters suggestParams = new SuggestParameters();
        suggestParams.orderBy(new LinkedList<>(Collections.singletonList("This is not a valid orderby.")));
        PagedIterable<SuggestResult> suggestResult = client.suggest("hotel",
            "sg",
            suggestParams,
            null);
        suggestResult.iterableByPage().iterator().next();
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
}
