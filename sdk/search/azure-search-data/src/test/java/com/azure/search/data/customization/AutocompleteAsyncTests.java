// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.data.customization;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.PagedFlux;
import com.azure.search.data.SearchIndexAsyncClient;
import com.azure.search.data.generated.models.AutocompleteItem;
import com.azure.search.data.generated.models.AutocompleteMode;
import com.azure.search.data.generated.models.AutocompleteParameters;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.Assert;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.azure.search.data.customization.SearchTestBase.HOTELS_INDEX_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AutocompleteAsyncTests extends AutocompleteTestBase {

    private SearchIndexAsyncClient client;

    @Override
    protected void initializeClient() {
        client = builderSetup().indexName(HOTELS_INDEX_NAME).buildAsyncClient();
        uploadDocumentsJson(client, HOTELS_INDEX_NAME, HOTELS_DATA_JSON);
    }

    @Override
    public void autocompleteThrowsWhenGivenBadSuggesterName() throws Exception {
        AutocompleteParameters params = new AutocompleteParameters();
        params.autocompleteMode(AutocompleteMode.ONE_TERM);

        PagedFlux<AutocompleteItem> results = client.autocomplete("very po", "Invalid suggester", null, params);
        StepVerifier
                .create(results)
                .verifyErrorSatisfies(error -> {
                    assertEquals(HttpResponseException.class, error.getClass());
                    assertEquals(HttpResponseStatus.BAD_REQUEST.code(), ((HttpResponseException) error).response().statusCode());
                    assertTrue(error.getMessage().contains("The specified suggester name 'Invalid suggester' does not exist in this index definition.\\r\\nParameter name: name"));
                });
    }

    @Override
    public void autocompleteDefaultsToOneTermMode() throws Exception {
        List<String> expectedText = Arrays.asList("point", "police", "polite", "pool", "popular");
        List<String> expectedQueryPlusText = Arrays.asList("point", "police", "polite", "pool", "popular");

        PagedFlux<AutocompleteItem> results = client.autocomplete("po", "sg");
        validateResults(expectedText, expectedQueryPlusText, results);
    }

    @Override
    public void autocompleteExcludesFieldsNotInSuggester() throws Exception {
        AutocompleteParameters params = new AutocompleteParameters();
        params.autocompleteMode(AutocompleteMode.ONE_TERM);
        params.searchFields(Arrays.asList("HotelName"));

        PagedFlux<AutocompleteItem> results = client.autocomplete("luxu", "sg", null, params);
        Assert.assertNotNull(results);

        StepVerifier.create(results.byPage()).assertNext(pageResult -> {
            Assert.assertEquals(0, pageResult.items().size());
        }).verifyComplete();
    }

    @Override
    public void autocompleteFuzzyIsOffByDefault() throws Exception {
        AutocompleteParameters params = new AutocompleteParameters();
        params.autocompleteMode(AutocompleteMode.ONE_TERM);

        PagedFlux<AutocompleteItem> results = client.autocomplete("pi", "sg", null, params);
        Assert.assertNotNull(results);
        StepVerifier.create(results.byPage()).assertNext(pageResult -> {
            Assert.assertEquals(0, pageResult.items().size());
        }).verifyComplete();
    }

    @Override
    public void autocompleteOneTerm() throws Exception {
        List<String> expectedText = Arrays.asList("point", "police", "polite", "pool", "popular");
        List<String> expectedQueryPlusText = Arrays.asList("point", "police", "polite", "pool", "popular");

        AutocompleteParameters params = new AutocompleteParameters();
        params.autocompleteMode(AutocompleteMode.ONE_TERM);

        PagedFlux<AutocompleteItem> results = client.autocomplete("po", "sg", null, params);
        validateResults(expectedText, expectedQueryPlusText, results);
    }

    @Override
    public void autocompleteStaticallyTypedDocuments() throws Exception {
        List<String> expectedText = Arrays.asList("point", "police", "polite", "pool", "popular");
        List<String> expectedQueryPlusText = Arrays.asList("very point", "very police", "very polite", "very pool", "very popular");

        AutocompleteParameters params = new AutocompleteParameters();
        params.autocompleteMode(AutocompleteMode.ONE_TERM);
        params.useFuzzyMatching(false);

        PagedFlux<AutocompleteItem> results = client.autocomplete("very po", "sg", null, params);
        validateResults(expectedText, expectedQueryPlusText, results);
    }

    @Override
    public void autocompleteThrowsWhenRequestIsMalformed() throws Exception {
        PagedFlux<AutocompleteItem> results = client.autocomplete("very po", "");
        StepVerifier
                .create(results)
                .verifyErrorSatisfies(error -> {
                    assertEquals(HttpResponseException.class, error.getClass());
                    assertEquals(HttpResponseStatus.BAD_REQUEST.code(), ((HttpResponseException) error).response().statusCode());
                    assertTrue(error.getMessage().contains("Cannot find fields enabled for suggestions. Please provide a value for 'suggesterName' in the query.\\r\\nParameter name: suggestions"));
                });
    }

    /**
     * Validate the text and query plus text results
     * @param expectedText
     * @param expectedQueryPlusText
     * @param results
     */
    private void validateResults(List<String> expectedText, List<String> expectedQueryPlusText, PagedFlux<AutocompleteItem> results) {
        StepVerifier.create(results.byPage()).assertNext(pageResult -> {
            Assert.assertEquals(5, pageResult.items().size());
            List<String> textResults = new ArrayList<>();
            List<String> queryPlusTextResults = new ArrayList<>();
            pageResult.items()
                    .forEach(res -> {
                        textResults.add(res.text());
                        queryPlusTextResults.add(res.queryPlusText());
                    });
            Assert.assertEquals(expectedText, textResults);
            Assert.assertEquals(expectedQueryPlusText, queryPlusTextResults);
        }).verifyComplete();
    }
}
