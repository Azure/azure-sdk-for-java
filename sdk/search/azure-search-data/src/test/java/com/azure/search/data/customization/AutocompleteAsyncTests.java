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
    public void canAutocompleteThrowsWhenGivenBadSuggesterName() {
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
    public void canAutocompleteDefaultsToOneTermMode() {
        List<String> expectedText = Arrays.asList("point", "police", "polite", "pool", "popular");
        List<String> expectedQueryPlusText = Arrays.asList("point", "police", "polite", "pool", "popular");

        PagedFlux<AutocompleteItem> results = client.autocomplete("po", "sg");
        validateResults(expectedText, expectedQueryPlusText, results);
    }

    @Override
    public void canAutocompleteExcludesFieldsNotInSuggester() {
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
    public void canAutocompleteFuzzyIsOffByDefault() {
        AutocompleteParameters params = new AutocompleteParameters();
        params.autocompleteMode(AutocompleteMode.ONE_TERM);

        PagedFlux<AutocompleteItem> results = client.autocomplete("pi", "sg", null, params);
        Assert.assertNotNull(results);
        StepVerifier.create(results.byPage()).assertNext(pageResult -> {
            Assert.assertEquals(0, pageResult.items().size());
        }).verifyComplete();
    }

    @Override
    public void canAutocompleteOneTerm() {
        List<String> expectedText = Arrays.asList("point", "police", "polite", "pool", "popular");
        List<String> expectedQueryPlusText = Arrays.asList("point", "police", "polite", "pool", "popular");

        AutocompleteParameters params = new AutocompleteParameters();
        params.autocompleteMode(AutocompleteMode.ONE_TERM);

        PagedFlux<AutocompleteItem> results = client.autocomplete("po", "sg", null, params);
        validateResults(expectedText, expectedQueryPlusText, results);
    }

    @Override
    public void canAutocompleteOneTermWithContext() {
        List<String> expectedText = Arrays.asList("very police", "very polite", "very popular");
        List<String> expectedQueryPlusText = Arrays.asList("looking for very police", "looking for very polite", "looking for very popular");

        AutocompleteParameters params = new AutocompleteParameters();
        params.autocompleteMode(AutocompleteMode.ONE_TERM_WITH_CONTEXT);

        PagedFlux<AutocompleteItem> results = client.autocomplete("looking for very po", "sg", null, params);
        validateResults(expectedText, expectedQueryPlusText, results);
    }

    @Override
    public void canAutocompleteStaticallyTypedDocuments() {
        List<String> expectedText = Arrays.asList("point", "police", "polite", "pool", "popular");
        List<String> expectedQueryPlusText = Arrays.asList("very point", "very police", "very polite", "very pool", "very popular");

        AutocompleteParameters params = new AutocompleteParameters();
        params.autocompleteMode(AutocompleteMode.ONE_TERM);
        params.useFuzzyMatching(false);

        PagedFlux<AutocompleteItem> results = client.autocomplete("very po", "sg", null, params);
        validateResults(expectedText, expectedQueryPlusText, results);
    }

    @Override
    public void canAutocompleteThrowsWhenRequestIsMalformed() {
        PagedFlux<AutocompleteItem> results = client.autocomplete("very po", "");
        StepVerifier
                .create(results)
                .verifyErrorSatisfies(error -> {
                    assertEquals(HttpResponseException.class, error.getClass());
                    assertEquals(HttpResponseStatus.BAD_REQUEST.code(), ((HttpResponseException) error).response().statusCode());
                    assertTrue(error.getMessage().contains("Cannot find fields enabled for suggestions. Please provide a value for 'suggesterName' in the query.\\r\\nParameter name: suggestions"));
                });
    }

    @Override
    public void canAutocompleteTwoTerms() {
        List<String> expectedText = Arrays.asList("point motel", "police station", "polite staff", "pool a", "popular hotel");
        List<String> expectedQueryPlusText = Arrays.asList("point motel", "police station", "polite staff", "pool a", "popular hotel");

        AutocompleteParameters params = new AutocompleteParameters();
        params.autocompleteMode(AutocompleteMode.TWO_TERMS);

        PagedFlux<AutocompleteItem> results = client.autocomplete("po", "sg", null, params);
        validateResults(expectedText, expectedQueryPlusText, results);
    }

    @Override
    public void testAutocompleteCanUseHitHighlighting() throws Exception {
        List<String> expectedText = Arrays.asList("pool", "popular");
        List<String> expectedQueryPlusText = Arrays.asList("<b>pool</b>", "<b>popular</b>");

        AutocompleteParameters params = new AutocompleteParameters()
            .autocompleteMode(AutocompleteMode.ONE_TERM)
            .filter("HotelName eq 'EconoStay' or HotelName eq 'Fancy Stay'")
            .highlightPreTag("<b>")
            .highlightPostTag("</b>");

        PagedFlux<AutocompleteItem> results = client.autocomplete("po", "sg", null, params);

        Assert.assertNotNull(results);
        validateResults(expectedText, expectedQueryPlusText, results);
    }

    @Override
    public void testAutocompleteWithMultipleSelectedFields() throws Exception {
        List<String> expectedText = Arrays.asList("model", "modern");
        List<String> expectedQueryPlusText = Arrays.asList("model", "modern");
        AutocompleteParameters params = new AutocompleteParameters()
            .autocompleteMode(AutocompleteMode.ONE_TERM)
            .searchFields(Arrays.asList("HotelName", "Description"));

        PagedFlux<AutocompleteItem> results = client.autocomplete("mod", "sg", null, params);

        Assert.assertNotNull(results);
        validateResults(expectedText, expectedQueryPlusText, results);
    }

    @Override
    public void testAutocompleteWithSelectedFields() throws Exception {
        List<String> expectedText = Arrays.asList("modern");
        List<String> expectedQueryPlusText = Arrays.asList("modern");

        AutocompleteParameters params = new AutocompleteParameters()
            .autocompleteMode(AutocompleteMode.ONE_TERM)
            .searchFields(Arrays.asList("HotelName"))
            .filter("HotelId eq '7'");

        PagedFlux<AutocompleteItem> results = client.autocomplete("mod", "sg", null, params);

        Assert.assertNotNull(results);
        validateResults(expectedText, expectedQueryPlusText, results);
    }

    @Override
    public void testAutocompleteTopTrimsResults() throws Exception {
        List<String> expectedText = Arrays.asList("point", "police");
        List<String> expectedQueryPlusText = Arrays.asList("point", "police");

        AutocompleteParameters params = new AutocompleteParameters()
            .autocompleteMode(AutocompleteMode.ONE_TERM)
            .top(2);

        PagedFlux<AutocompleteItem> results = client.autocomplete("po", "sg", null, params);

        Assert.assertNotNull(results);
        validateResults(expectedText, expectedQueryPlusText, results);
    }

    @Override
    public void testAutocompleteWithFilter() throws Exception {
        List<String> expectedText = Arrays.asList("polite");
        List<String> expectedQueryPlusText = Arrays.asList("polite");

        AutocompleteParameters params = new AutocompleteParameters()
            .autocompleteMode(AutocompleteMode.ONE_TERM)
            .filter("search.in(HotelId, '6,7')");

        PagedFlux<AutocompleteItem> results = client.autocomplete("po", "sg", null, params);

        Assert.assertNotNull(results);
        validateResults(expectedText, expectedQueryPlusText, results);
    }

    @Override
    public void testAutocompleteOneTermWithContextWithFuzzy() throws Exception {
        List<String> expectedText = Arrays.asList("very polite", "very police");
        List<String> expectedQueryPlusText = Arrays.asList("very polite", "very police");

        AutocompleteParameters params = new AutocompleteParameters()
            .autocompleteMode(AutocompleteMode.ONE_TERM_WITH_CONTEXT)
            .useFuzzyMatching(true);

        PagedFlux<AutocompleteItem> results = client.autocomplete("very polit", "sg", null, params);

        Assert.assertNotNull(results);
        validateResults(expectedText, expectedQueryPlusText, results);
    }

    @Override
    public void testAutocompleteOneTermWithFuzzy() throws Exception {
        List<String> expectedText = Arrays.asList("model", "modern", "morel", "motel");
        List<String> expectedQueryPlusText = Arrays.asList("model", "modern", "morel", "motel");

        AutocompleteParameters params = new AutocompleteParameters()
            .autocompleteMode(AutocompleteMode.ONE_TERM)
            .useFuzzyMatching(true);

        PagedFlux<AutocompleteItem> results = client.autocomplete("mod", "sg", null, params);

        Assert.assertNotNull(results);
        validateResults(expectedText, expectedQueryPlusText, results);
    }

    @Override
    public void testAutocompleteTwoTermsWithFuzzy() throws Exception {
        List<String> expectedText = Arrays.asList("model suites", "modern architecture", "modern stay", "morel coverings", "motel");
        List<String> expectedQueryPlusText = Arrays.asList("model suites", "modern architecture", "modern stay", "morel coverings", "motel");

        AutocompleteParameters params = new AutocompleteParameters()
            .autocompleteMode(AutocompleteMode.TWO_TERMS)
            .useFuzzyMatching(true);

        PagedFlux<AutocompleteItem> results = client.autocomplete("mod", "sg", null, params);

        Assert.assertNotNull(results);
        validateResults(expectedText, expectedQueryPlusText, results);
    }

    @Override
    public void testAutocompleteWithFilterAndFuzzy() throws Exception {
        List<String> expectedText = Arrays.asList("modern", "motel");
        List<String> expectedQueryPlusText = Arrays.asList("modern", "motel");

        AutocompleteParameters params = new AutocompleteParameters()
            .autocompleteMode(AutocompleteMode.ONE_TERM)
            .useFuzzyMatching(true)
            .filter("HotelId ne '6' and (HotelName eq 'Modern Stay' or Tags/any(t : t eq 'budget'))");

        PagedFlux<AutocompleteItem> results = client.autocomplete("mod", "sg", null, params);

        Assert.assertNotNull(results);
        validateResults(expectedText, expectedQueryPlusText, results);
    }

    /**
     * Validate the text and query plus text results
     * @param expectedText
     * @param expectedQueryPlusText
     * @param results
     */
    private void validateResults(List<String> expectedText, List<String> expectedQueryPlusText, PagedFlux<AutocompleteItem> results) {
        StepVerifier.create(results.byPage()).assertNext(pageResult -> {
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
